package otpproju.service;

import org.mindrot.jbcrypt.BCrypt;
import otpproju.model.User;
import otpproju.repository.UserRepository;
import otpproju.repository.RepositoryException;

import java.util.List;
import java.util.NoSuchElementException;
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
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = Objects.requireNonNull(
                repository, "User repository must not be null");
    }

    public User getUser(int userId) {
        validatePositiveId(userId, "User ID");
        return findOrThrow(userId);
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public User updateProfile(int userId, String username, String email) {
        validatePositiveId(userId, "User ID");
        User user = findOrThrow(userId);

        String cleanedUsername = validateUsername(username);
        String cleanedEmail = validateEmail(email);

        ensureUsernameAvailable(cleanedUsername, userId);
        ensureEmailAvailable(cleanedEmail, userId);

        user.setUsername(cleanedUsername);
        user.setEmail(cleanedEmail);

        if (!repository.update(user)) {
            throw new RepositoryException("User profile could not be updated");
        }

        return repository.findById(userId).orElseThrow(
                () -> new RepositoryException("Updated user could not be retrieved"));
    }

    public void changePassword(int userId, String currentPassword, String newPassword) {
        validatePositiveId(userId, "User ID");

        if (currentPassword == null || currentPassword.isEmpty()) {
            throw new AuthenticationException("Current password is incorrect");
        }

        validatePassword(newPassword);
        User user = findOrThrow(userId);

        if (!passwordMatches(currentPassword, user)) {
            throw new AuthenticationException("Current password is incorrect");
        }

        if (!repository.updatePasswordHash(userId, hashPassword(newPassword))) {
            throw new RepositoryException("Password could not be updated");
        }
    }

    public void deleteUser(int userId) {
        validatePositiveId(userId, "User ID");

        if (!repository.deleteById(userId)) {
            throw new NoSuchElementException("User was not found");
        }
    }

    public User registerUser(String username, String email, String password) {
        String cleanedUsername = validateUsername(username);
        String cleanedEmail = validateEmail(email);
        validatePassword(password);

        ensureUsernameAvailable(cleanedUsername, null);
        ensureEmailAvailable(cleanedEmail, null);

        User user = new User(
                cleanedUsername,
                cleanedEmail,
                hashPassword(password),
                User.UserType.STUDENT);

        return repository.create(user);
    }

    public User login(String email, String password) {
        String cleanedEmail = validateEmail(email);

        return repository.findByEmail(cleanedEmail)
                .filter(user -> passwordMatches(password, user))
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));
    }

    private User findOrThrow(int userId) {
        return repository.findById(userId).orElseThrow(
                () -> new NoSuchElementException("User was not found"));
    }

    private void ensureUsernameAvailable(String username, Integer currentUserId) {
        repository.findByUsername(username)
                .filter(owner -> !owner.getUserId().equals(currentUserId))
                .ifPresent(owner -> {
                    throw new IllegalArgumentException("Username is already in use");
                });
    }

    private void ensureEmailAvailable(String email, Integer currentUserId) {
        repository.findByEmail(email)
                .filter(owner -> !owner.getUserId().equals(currentUserId))
                .ifPresent(owner -> {
                    throw new IllegalArgumentException("Email address is already in use");
                });
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST));
    }

    private boolean passwordMatches(String rawPassword, User user) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, user.getPasswordHash());
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }


    private void validatePositiveId(int id, String fieldName) {
        if (id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }

    private String validateUsername(String username) {
        requireNotBlank(username, "Username");
        String cleaned = username.trim();
        requireMaxLength(cleaned, MAX_USERNAME_LENGTH, "Username");
        return cleaned;
    }

    private String validateEmail(String email) {
        requireNotBlank(email, "Email");
        String cleaned = email.trim().toLowerCase(Locale.ROOT);
        requireMaxLength(cleaned, MAX_EMAIL_LENGTH, "Email");

        if (!EMAIL_PATTERN.matcher(cleaned).matches()) {
            throw new IllegalArgumentException("Email address is invalid");
        }
        return cleaned;
    }

    private void validatePassword(String password) {
        requireNotBlank(password, "Password");

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must contain at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        requireMaxLength(password, MAX_PASSWORD_LENGTH, "Password");
    }

    private void requireNotBlank(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be empty");
        }
    }

    private void requireMaxLength(String value, int max, String label) {
        if (value.length() > max) {
            throw new IllegalArgumentException(
                    label + " must not exceed " + max + " characters");
        }
    }
}