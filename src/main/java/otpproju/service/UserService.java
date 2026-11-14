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

// this class handles user business logic: registration, login, profile updates,
// password changes and deletion. Validation and hashing happen here,
// storage is delegated to UserRepository.
public class UserService {

    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MAX_EMAIL_LENGTH = 255;

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 72;   // BCrypt only uses the first 72 bytes

    private static final int BCRYPT_COST = 12;   // higher = slower hashing = harder to brute force

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository repository;

    // Creates the service with the repository it uses for data access.
    public UserService(UserRepository repository) {
        this.repository = Objects.requireNonNull(
                repository, "User repository must not be null");   // throws NullPointerException if repository is null
    }

    // ---------- public operations ----------

    // Returns the user with the given ID.
    public User getUser(int userId) {
        validatePositiveId(userId, "User ID");   // throws IllegalArgumentException if userId is 0 or negative
        return findOrThrow(userId);              // throws NoSuchElementException if no user has this ID
    }

    // Returns all users.
    public List<User> getAllUsers() {
        return repository.findAll();
    }

    // Updates a user's username and email, then returns the saved user.
    // Both values are validated and must not belong to another user.
    public User updateProfile(int userId, String username, String email) {
        validatePositiveId(userId, "User ID");   // throws IllegalArgumentException if userId is 0 or negative
        User user = findOrThrow(userId);         // throws NoSuchElementException if no user has this ID

        String cleanedUsername = validateUsername(username);   // throws IllegalArgumentException if invalid
        String cleanedEmail = validateEmail(email);            // throws IllegalArgumentException if invalid

        ensureUsernameAvailable(cleanedUsername, userId);   // throws IllegalArgumentException if another user has this username
        ensureEmailAvailable(cleanedEmail, userId);         // throws IllegalArgumentException if another user has this email

        user.setUsername(cleanedUsername);   // changes the object in memory only, not saved yet
        user.setEmail(cleanedEmail);

        if (!repository.update(user)) {   // update returns false if the save failed
            throw new RepositoryException("User profile could not be updated");   // throws RepositoryException
        }

        return repository.findById(userId).orElseThrow(   // reloads the saved user from the database
                () -> new RepositoryException("Updated user could not be retrieved"));   // throws RepositoryException if it can't be reloaded
    }

    // Changes a user's password after checking the current one.
    // The new password is validated and stored as a BCrypt hash.
    public void changePassword(int userId, String currentPassword, String newPassword) {
        validatePositiveId(userId, "User ID");   // throws IllegalArgumentException if userId is 0 or negative

        if (currentPassword == null || currentPassword.isEmpty()) {
            throw new AuthenticationException("Current password is incorrect");   // throws AuthenticationException if current password is missing
        }

        validatePassword(newPassword);     // throws IllegalArgumentException if new password is blank, too short or too long
        User user = findOrThrow(userId);   // throws NoSuchElementException if no user has this ID

        if (!passwordMatches(currentPassword, user)) {   // compares against the stored hash
            throw new AuthenticationException("Current password is incorrect");   // throws AuthenticationException if it doesn't match
        }

        if (!repository.updatePasswordHash(userId, hashPassword(newPassword))) {   // updatePasswordHash returns false if the save failed
            throw new RepositoryException("Password could not be updated");        // throws RepositoryException
        }
    }

    // Deletes the user with the given ID.
    public void deleteUser(int userId) {
        validatePositiveId(userId, "User ID");   // throws IllegalArgumentException if userId is 0 or negative

        if (!repository.deleteById(userId)) {                          // deleteById returns false if no row was deleted
            throw new NoSuchElementException("User was not found");   // throws NoSuchElementException if no user has this ID
        }
    }

    // Registers a new STUDENT user and returns the saved user.
    // Validates the input, checks the username and email are free, hashes the password and saves.
    public User registerUser(String username, String email, String password) {
        String cleanedUsername = validateUsername(username);   // throws IllegalArgumentException if invalid
        String cleanedEmail = validateEmail(email);            // throws IllegalArgumentException if invalid
        validatePassword(password);                            // throws IllegalArgumentException if blank, too short or too long

        ensureUsernameAvailable(cleanedUsername, null);   // throws IllegalArgumentException if the username is taken (null = no current user)
        ensureEmailAvailable(cleanedEmail, null);         // throws IllegalArgumentException if the email is taken

        User user = new User(
                cleanedUsername,
                cleanedEmail,
                hashPassword(password),   // only the hash is stored, never the plain password
                User.UserType.STUDENT);   // new accounts are always students

        return repository.create(user);   // saves the user and returns it with its generated ID
    }

    // Logs a user in with email and password.
    // A missing user and a wrong password give the same error, so callers can't tell which failed.
    public User login(String email, String password) {
        String cleanedEmail = validateEmail(email);   // throws IllegalArgumentException if the email format is invalid

        return repository.findByEmail(cleanedEmail)                       // empty if no user has this email
                .filter(user -> passwordMatches(password, user))    // keeps the user only if the password matches
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));   // throws AuthenticationException if user missing or password wrong
    }

    // ---------- helpers ----------

    // Finds a user by ID or throws if there is none.
    private User findOrThrow(int userId) {
        return repository.findById(userId).orElseThrow(
                () -> new NoSuchElementException("User was not found"));   // throws NoSuchElementException if no user has this ID
    }

    // Throws if the username belongs to a different user.
    // currentUserId is the user being edited, or null when registering.
    private void ensureUsernameAvailable(String username, Integer currentUserId) {
        repository.findByUsername(username)                                         // empty if nobody has this username
                .filter(owner -> !owner.getUserId().equals(currentUserId))    // ignores the user's own current username
                .ifPresent(owner -> {
                    throw new IllegalArgumentException("Username is already in use");   // throws IllegalArgumentException if someone else has it
                });
    }

    // Throws if the email belongs to a different user.
    // currentUserId is the user being edited, or null when registering.
    private void ensureEmailAvailable(String email, Integer currentUserId) {
        repository.findByEmail(email)                                                 // empty if nobody has this email
                .filter(owner -> !owner.getUserId().equals(currentUserId))      // ignores the user's own current email
                .ifPresent(owner -> {
                    throw new IllegalArgumentException("Email address is already in use");   // throws IllegalArgumentException if someone else has it
                });
    }

    // Hashes a plain-text password with BCrypt using the configured cost.
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST));   // gensalt creates a new random salt (a short piece of random data) each time
    }

    // Checks a plain-text password against the user's stored hash.
    // Returns false instead of throwing for missing input or a malformed stored hash.
    private boolean passwordMatches(String rawPassword, User user) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            return false;   // nothing to compare
        }
        try {
            return BCrypt.checkpw(rawPassword, user.getPasswordHash());   // true if the password produces the stored hash
        } catch (IllegalArgumentException exception) {
            return false;   // the stored hash isn't valid BCrypt, so treat it as a mismatch
        }
    }

    // ---------- validation ----------

    // Throws if the ID is zero or negative. fieldName is used in the error message.
    private void validatePositiveId(int id, String fieldName) {
        if (id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");   // throws IllegalArgumentException
        }
    }

    // Checks the username is not blank or too long, and returns it trimmed.
    private String validateUsername(String username) {
        requireNotBlank(username, "Username");   // throws IllegalArgumentException if null or blank
        String cleaned = username.trim();             // removes spaces at the start and end
        requireMaxLength(cleaned, MAX_USERNAME_LENGTH, "Username");   // throws IllegalArgumentException if longer than 50
        return cleaned;
    }

    // Checks the email is not blank or too long and has a valid format.
    // Returns it trimmed and in lower case.
    private String validateEmail(String email) {
        requireNotBlank(email, "Email");   // throws IllegalArgumentException if null or blank
        String cleaned = email.trim().toLowerCase(Locale.ROOT);   // so "MYNAME@GMAIL.com" and "myname@gmail.com" count as the same email
        requireMaxLength(cleaned, MAX_EMAIL_LENGTH, "Email");   // throws IllegalArgumentException if longer than 255

        if (!EMAIL_PATTERN.matcher(cleaned).matches()) {
            throw new IllegalArgumentException("Email address is invalid");   // throws IllegalArgumentException if the format is wrong
        }
        return cleaned;
    }

    // Checks the password is not blank and is between 6 and 72 characters.
    private void validatePassword(String password) {
        requireNotBlank(password, "Password");   // throws IllegalArgumentException if null or blank

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must contain at least " + MIN_PASSWORD_LENGTH + " characters");   // throws IllegalArgumentException if shorter than 6
        }
        requireMaxLength(password, MAX_PASSWORD_LENGTH, "Password");   // throws IllegalArgumentException if longer than 72
    }

    // Throws if the value is null or blank
    private void requireNotBlank(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be empty");   // throws IllegalArgumentException
        }
    }

    // Throws if the value is longer than max
    private void requireMaxLength(String value, int max, String label) {
        if (value.length() > max) {
            throw new IllegalArgumentException(
                    label + " must not exceed " + max + " characters");   // throws IllegalArgumentException
        }
    }
}