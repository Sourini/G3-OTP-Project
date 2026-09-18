package otpproju.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import otpproju.model.User;
import otpproju.repository.UserRepository;

import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class UserServiceTest {

    private UserRepository repository;
    private UserService service;

    private List<Integer> createdUserIds;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
        service = new UserService(repository);
        createdUserIds = new ArrayList<>();
    }

    @AfterEach
    void cleanUp() {
        for (Integer userId : createdUserIds) {
            repository.deleteById(userId);
        }
    }

    @Test
    void registersAndPersistsStudent() {
        String uniqueValue = UUID.randomUUID().toString();

        User user = registerTestUser(
                "student_" + uniqueValue,
                "student_" + uniqueValue + "@example.com",
                "secret123"
        );

        assertNotNull(user.getUserId());
        assertNotNull(user.getCreatedAt());

        assertEquals(
                User.UserType.STUDENT,
                user.getUserType()
        );

        assertEquals(
                "student_" + uniqueValue,
                user.getUsername()
        );

        assertEquals(
                "student_" + uniqueValue + "@example.com",
                user.getEmail()
        );
    }

    @Test
    void registrationTrimsUsernameAndNormalizesEmail() {
        String uniqueValue = UUID.randomUUID().toString();

        User user = registerTestUser(
                "  student_" + uniqueValue + "  ",
                "  Student_" + uniqueValue +
                        "@EXAMPLE.COM  ",
                "secret123"
        );

        assertEquals(
                "student_" + uniqueValue,
                user.getUsername()
        );

        assertEquals(
                "student_" + uniqueValue +
                        "@example.com",
                user.getEmail()
        );
    }

    @Test
    void registrationHashesPassword() {
        String uniqueValue = UUID.randomUUID().toString();
        String plainPassword = "secret123";

        User user = registerTestUser(
                "hash_" + uniqueValue,
                "hash_" + uniqueValue + "@example.com",
                plainPassword
        );

        assertNotEquals(
                plainPassword,
                user.getPasswordHash()
        );

        assertTrue(
                user.getPasswordHash().startsWith("$2")
        );
    }

    @Test
    void loginSucceedsWithCorrectCredentials() {
        String uniqueValue = UUID.randomUUID().toString();

        User registered = registerTestUser(
                "login_" + uniqueValue,
                "login_" + uniqueValue + "@example.com",
                "secret123"
        );

        User loggedIn = service.login(
                registered.getEmail(),
                "secret123"
        );

        assertEquals(registered.getUserId(), loggedIn.getUserId());
        assertEquals(registered.getEmail(), loggedIn.getEmail());
    }

    @Test
    void loginNormalizesEmail() {
        String uniqueValue = UUID.randomUUID().toString();

        User registered = registerTestUser(
                "case_" + uniqueValue,
                "case_" + uniqueValue + "@example.com",
                "secret123"
        );

        User loggedIn = service.login(
                "  " + registered.getEmail().toUpperCase() + "  ",
                "secret123"
        );

        assertEquals(registered.getUserId(), loggedIn.getUserId());
    }

    @Test
    void loginRejectsWrongPassword() {
        String uniqueValue = UUID.randomUUID().toString();

        User registered = registerTestUser(
                "wrong_" + uniqueValue,
                "wrong_" + uniqueValue + "@example.com",
                "secret123"
        );

        assertThrows(
                AuthenticationException.class,
                () -> service.login(
                        registered.getEmail(),
                        "incorrect-password"
                )
        );
    }

    @Test
    void loginRejectsUnknownEmail() {
        assertThrows(
                AuthenticationException.class,
                () -> service.login(
                        UUID.randomUUID() + "@example.com",
                        "secret123"
                )
        );
    }

    @Test
    void registrationRejectsDuplicateUsername() {
        String uniqueValue = UUID.randomUUID().toString();

        registerTestUser(
                "duplicate_" + uniqueValue,
                "first_" + uniqueValue + "@example.com",
                "secret123"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerUser(
                        "duplicate_" + uniqueValue,
                        "second_" + uniqueValue +
                                "@example.com",
                        "secret123"
                )
        );
    }

    @Test
    void registrationRejectsDuplicateEmail() {
        String uniqueValue = UUID.randomUUID().toString();

        registerTestUser(
                "first_" + uniqueValue,
                "duplicate_" + uniqueValue +
                        "@example.com",
                "secret123"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerUser(
                        "second_" + uniqueValue,
                        "duplicate_" + uniqueValue +
                                "@example.com",
                        "secret123"
                )
        );
    }

    @Test
    void registrationRejectsBlankUsername() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerUser(
                        "   ",
                        "student@example.com",
                        "secret123"
                )
        );
    }

    @Test
    void registrationRejectsUsernameLongerThan50Characters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerUser(
                        "a".repeat(51),
                        "student@example.com",
                        "secret123"
                )
        );
    }

    @Test
    void registrationRejectsInvalidEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerUser(
                        "student",
                        "not-an-email",
                        "secret123"
                )
        );
    }

    @Test
    void registrationRejectsShortPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerUser(
                        "student",
                        "student@example.com",
                        "12345"
                )
        );
    }

    @Test
    void registrationRejectsPasswordLongerThan72Characters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerUser(
                        "student",
                        "student@example.com",
                        "a".repeat(73)
                )
        );
    }

    @Test
    void loginRejectsEmptyPassword() {
        assertThrows(
                AuthenticationException.class,
                () -> service.login(
                        "student@example.com",
                        ""
                )
        );
    }

    @Test
    void constructorRejectsNullRepository() {
        assertThrows(
                NullPointerException.class,
                () -> new UserService(null)
        );
    }

    private User registerTestUser(
            String username,
            String email,
            String password
    ) {
        User user = service.registerUser(
                username,
                email,
                password
        );

        createdUserIds.add(user.getUserId());

        return user;
    }

    @Test
    void updatesUserProfile() {
        String uniqueValue = UUID.randomUUID().toString();

        User user = registerTestUser(
                "profile_" + uniqueValue,
                "profile_" + uniqueValue + "@example.com",
                "secret123"
        );

        User updated = service.updateProfile(
                user.getUserId(),
                "updated_" + uniqueValue,
                "UPDATED_" + uniqueValue + "@EXAMPLE.COM"
        );

        assertEquals(
                "updated_" + uniqueValue,
                updated.getUsername()
        );

        assertEquals(
                "updated_" + uniqueValue + "@example.com",
                updated.getEmail()
        );
    }

    @Test
    void changesPassword() {
        String uniqueValue = UUID.randomUUID().toString();

        User user = registerTestUser(
                "password_" + uniqueValue,
                "password_" + uniqueValue + "@example.com",
                "old-password"
        );

        service.changePassword(
                user.getUserId(),
                "old-password",
                "new-password"
        );

        assertThrows(
                AuthenticationException.class,
                () -> service.login(
                        user.getEmail(),
                        "old-password"
                )
        );

        User loggedIn = service.login(
                user.getEmail(),
                "new-password"
        );

        assertEquals(user.getUserId(), loggedIn.getUserId());
    }

    @Test
    void passwordChangeRejectsIncorrectCurrentPassword() {
        String uniqueValue = UUID.randomUUID().toString();

        User user = registerTestUser(
                "wrong_current_" + uniqueValue,
                "wrong_current_" + uniqueValue +
                        "@example.com",
                "old-password"
        );

        assertThrows(
                AuthenticationException.class,
                () -> service.changePassword(
                        user.getUserId(),
                        "incorrect-password",
                        "new-password"
                )
        );
    }

    @Test
    void deletesUserAccount() {
        String uniqueValue = UUID.randomUUID().toString();

        User user = registerTestUser(
                "delete_" + uniqueValue,
                "delete_" + uniqueValue + "@example.com",
                "secret123"
        );

        service.deleteUser(user.getUserId());

        createdUserIds.remove(user.getUserId());

        assertThrows(
                NoSuchElementException.class,
                () -> service.getUser(user.getUserId())
        );
    }
}