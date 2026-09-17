package otpproju.service;

import otpproju.model.User;

import java.util.Locale;
import java.util.regex.Pattern;

public class UserService {

    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MAX_EMAIL_LENGTH = 255;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public User createUser(
            String username,
            String email,
            String passwordHash,
            User.UserType userType
    ) {
        String cleanedUsername = validateUsername(username);
        String cleanedEmail = validateEmail(email);

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Password hash must not be empty"
            );
        }

        if (userType == null) {
            throw new IllegalArgumentException(
                    "User type must not be null"
            );
        }

        return new User(
                cleanedUsername,
                cleanedEmail,
                passwordHash,
                userType
        );
    }

    private String validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username must not be empty"
            );
        }

        String cleanedUsername = username.trim();

        if (cleanedUsername.length() > MAX_USERNAME_LENGTH) {
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

        if (!EMAIL_PATTERN.matcher(cleanedEmail).matches()) {
            throw new IllegalArgumentException(
                    "Email address is invalid"
            );
        }

        return cleanedEmail;
    }
}