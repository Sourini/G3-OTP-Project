package otpproju.service;

import otpproju.model.User;

public class UserService {

    public User createUser(
            String username,
            String passwordHash,
            User.UserType userType
    ) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username must not be empty"
            );
        }

        if (username.length() > 50) {
            throw new IllegalArgumentException(
                    "Username must not exceed 50 characters"
            );
        }

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
                username.trim(),
                passwordHash,
                userType
        );
    }
}