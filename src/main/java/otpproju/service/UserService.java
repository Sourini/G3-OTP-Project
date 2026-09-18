package otpproju.service;

import org.mindrot.jbcrypt.BCrypt;
import otpproju.model.User;
import otpproju.repository.UserRepository;
import otpproju.repository.RepositoryException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class UserService {

    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MAX_EMAIL_LENGTH = 255;

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 72;

    private static final int BCRYPT_COST = 12;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[A-Za-z0-9._%+-]+@" +
                            "[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            );

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = Objects.requireNonNull(
                repository,
                "User repository must not be null"
        );
    }

    public User getUser(int userId) {
        validatePositiveId(userId, "User ID");

        return repository.findById(userId).orElseThrow(
                () -> new NoSuchElementException(
                        "User was not found"
                )
        );
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public User updateProfile(
            int userId,
            String username,
            String email
    ) {
        validatePositiveId(userId, "User ID");

        User user = repository
                .findById(userId)
                .orElseThrow(
                        () -> new NoSuchElementException(
                                "User was not found"
                        )
                );

        String cleanedUsername =
                validateUsername(username);

        String cleanedEmail =
                validateEmail(email);

        Optional<User> usernameOwner =
                repository.findByUsername(cleanedUsername);

        if (usernameOwner.isPresent()
                && !usernameOwner.get()
                .getUserId()
                .equals(userId)) {

            throw new IllegalArgumentException(
                    "Username is already in use"
            );
        }

        Optional<User> emailOwner =
                repository.findByEmail(cleanedEmail);

        if (emailOwner.isPresent()
                && !emailOwner.get()
                .getUserId()
                .equals(userId)) {

            throw new IllegalArgumentException(
                    "Email address is already in use"
            );
        }

        user.setUsername(cleanedUsername);
        user.setEmail(cleanedEmail);

        if (!repository.update(user)) {
            throw new RepositoryException(
                    "User profile could not be updated"
            );
        }

        return repository
                .findById(userId)
                .orElseThrow(
                        () -> new RepositoryException(
                                "Updated user could not be retrieved"
                        )
                );
    }

    public void changePassword(
            int userId,
            String currentPassword,
            String newPassword
    ) {
        validatePositiveId(userId, "User ID");

        if (currentPassword == null
                || currentPassword.isEmpty()) {

            throw new AuthenticationException(
                    "Current password is incorrect"
            );
        }

        validatePassword(newPassword);

        User user = repository
                .findById(userId)
                .orElseThrow(
                        () -> new NoSuchElementException(
                                "User was not found"
                        )
                );

        boolean currentPasswordMatches;

        try {
            currentPasswordMatches = BCrypt.checkpw(
                    currentPassword,
                    user.getPasswordHash()
            );
        } catch (IllegalArgumentException exception) {
            currentPasswordMatches = false;
        }

        if (!currentPasswordMatches) {
            throw new AuthenticationException(
                    "Current password is incorrect"
            );
        }

        String newPasswordHash = BCrypt.hashpw(
                newPassword,
                BCrypt.gensalt(BCRYPT_COST)
        );

        if (!repository.updatePasswordHash(
                userId,
                newPasswordHash
        )) {
            throw new RepositoryException(
                    "Password could not be updated"
            );
        }
    }

    public void deleteUser(int userId) {
        validatePositiveId(userId, "User ID");

        if (!repository.deleteById(userId)) {
            throw new NoSuchElementException(
                    "User was not found"
            );
        }
    }

    private void validatePositiveId(
            int id,
            String fieldName
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be positive"
            );
        }
    }

    public User registerUser(
            String username,
            String email,
            String password
    ) {
        String cleanedUsername =
                validateUsername(username);

        String cleanedEmail =
                validateEmail(email);

        validatePassword(password);

        if (repository
                .findByUsername(cleanedUsername)
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Username is already in use"
            );
        }

        if (repository
                .findByEmail(cleanedEmail)
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Email address is already in use"
            );
        }

        String passwordHash = BCrypt.hashpw(
                password,
                BCrypt.gensalt(BCRYPT_COST)
        );

        User user = new User(
                cleanedUsername,
                cleanedEmail,
                passwordHash,
                User.UserType.STUDENT
        );

        return repository.create(user);
    }

    public User login(
            String email,
            String password
    ) {
        String cleanedEmail =
                validateEmail(email);

        if (password == null || password.isEmpty()) {
            throw new AuthenticationException(
                    "Invalid email or password"
            );
        }

        User user = repository
                .findByEmail(cleanedEmail)
                .orElseThrow(
                        () -> new AuthenticationException(
                                "Invalid email or password"
                        )
                );

        boolean passwordMatches;

        try {
            passwordMatches = BCrypt.checkpw(
                    password,
                    user.getPasswordHash()
            );
        } catch (IllegalArgumentException exception) {
            passwordMatches = false;
        }

        if (!passwordMatches) {
            throw new AuthenticationException(
                    "Invalid email or password"
            );
        }

        return user;
    }

    private String validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username must not be empty"
            );
        }

        String cleanedUsername = username.trim();

        if (cleanedUsername.length() >
                MAX_USERNAME_LENGTH) {

            throw new IllegalArgumentException(
                    "Username must not exceed 50 characters"
            );
        }

        return cleanedUsername;
    }

    private String validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email must not be empty"
            );
        }

        String cleanedEmail = email
                .trim()
                .toLowerCase(Locale.ROOT);

        if (cleanedEmail.length() > MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException(
                    "Email must not exceed 255 characters"
            );
        }

        if (!EMAIL_PATTERN
                .matcher(cleanedEmail)
                .matches()) {

            throw new IllegalArgumentException(
                    "Email address is invalid"
            );
        }

        return cleanedEmail;
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Password must not be empty"
            );
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must contain at least 6 characters"
            );
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must not exceed 72 characters"
            );
        }
    }
}