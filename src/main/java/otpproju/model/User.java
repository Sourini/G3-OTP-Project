package otpproju.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class User {

    private Integer userId;
    private String username;
    private String email;
    private String passwordHash;
    private UserType userType;
    private LocalDateTime createdAt;

    public User() {
        this.userType = UserType.STUDENT;
    }

    public User(
            String username,
            String email,
            String passwordHash,
            UserType userType
    ) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.userType = userType;
    }

    public User(
            Integer userId,
            String username,
            String email,
            String passwordHash,
            UserType userType,
            LocalDateTime createdAt
    ) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.userType = userType;
        this.createdAt = createdAt;
    }

    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserType getUserType() {
        return userType;
    }
    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof User other)) {
            return false;
        }

        return userId != null && userId.equals(other.userId);
    }

    @Override
    public int hashCode() {
        return userId == null
                ? System.identityHashCode(this)
                : Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", userType=" + userType +
                ", createdAt=" + createdAt +
                '}';
    }

    public enum UserType {
        STUDENT,
        ADMIN,
        TEACHER;


        public static UserType fromDatabaseValue(String value) {
            if (value == null) {
                throw new IllegalArgumentException(
                        "Database user type must not be null"
                );
            }

            try {
                return UserType.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        "Unknown user type: " + value,
                        exception
                );
            }
        }


        public String toDatabaseValue() {
            return name().toLowerCase();
        }
    }
}