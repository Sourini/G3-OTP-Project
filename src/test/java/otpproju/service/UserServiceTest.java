package otpproju.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import otpproju.model.User;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService();
    }

    @Test
    void createUserCreatesStudentWithValidInput() {
        User user = service.createUser(
                "student1",
                "student1@example.com",
                "hashed-password",
                User.UserType.STUDENT
        );

        assertEquals("student1", user.getUsername());
        assertEquals(
                "student1@example.com",
                user.getEmail()
        );
        assertEquals(
                "hashed-password",
                user.getPasswordHash()
        );
        assertEquals(
                User.UserType.STUDENT,
                user.getUserType()
        );
    }

    @Test
    void createUserCreatesTeacherWithValidInput() {
        User user = service.createUser(
                "teacher1",
                "teacher1@example.com",
                "hashed-password",
                User.UserType.TEACHER
        );

        assertEquals("teacher1", user.getUsername());
        assertEquals(
                "teacher1@example.com",
                user.getEmail()
        );
        assertEquals(
                User.UserType.TEACHER,
                user.getUserType()
        );
    }

    @Test
    void createUserTrimsUsernameAndEmail() {
        User user = service.createUser(
                "  student1  ",
                "  student1@example.com  ",
                "hashed-password",
                User.UserType.STUDENT
        );

        assertEquals("student1", user.getUsername());
        assertEquals(
                "student1@example.com",
                user.getEmail()
        );
    }

    @Test
    void createUserConvertsEmailToLowercase() {
        User user = service.createUser(
                "student1",
                "Student1@EXAMPLE.COM",
                "hashed-password",
                User.UserType.STUDENT
        );

        assertEquals(
                "student1@example.com",
                user.getEmail()
        );
    }

    @Test
    void createUserDoesNotModifyPasswordHash() {
        String passwordHash = " hash with spaces ";

        User user = service.createUser(
                "student1",
                "student1@example.com",
                passwordHash,
                User.UserType.STUDENT
        );

        assertEquals(passwordHash, user.getPasswordHash());
    }

    @Test
    void createUserRejectsNullUsername() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createUser(
                        null,
                        "student1@example.com",
                        "hashed-password",
                        User.UserType.STUDENT
                )
        );
    }

    @Test
    void createUserRejectsBlankUsername() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createUser(
                        "   ",
                        "student1@example.com",
                        "hashed-password",
                        User.UserType.STUDENT
                )
        );
    }

    @Test
    void createUserAcceptsUsernameWithExactly50Characters() {
        String username = "a".repeat(50);

        User user = service.createUser(
                username,
                "student1@example.com",
                "hashed-password",
                User.UserType.STUDENT
        );

        assertEquals(50, user.getUsername().length());
    }

    @Test
    void createUserRejectsUsernameLongerThan50Characters() {
        String username = "a".repeat(51);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createUser(
                        username,
                        "student1@example.com",
                        "hashed-password",
                        User.UserType.STUDENT
                )
        );
    }

    @Test
    void createUserRejectsNullEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createUser(
                        "student1",
                        null,
                        "hashed-password",
                        User.UserType.STUDENT
                )
        );
    }

    @Test
    void createUserRejectsBlankEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createUser(
                        "student1",
                        "   ",
                        "hashed-password",
                        User.UserType.STUDENT
                )
        );
    }

    @Test
    void createUserRejectsInvalidEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createUser(
                        "student1",
                        "not-an-email",
                        "hashed-password",
                        User.UserType.STUDENT
                )
        );
    }

    @Test
    void createUserRejectsEmailLongerThan255Characters() {
        String email =
                "a".repeat(244) + "@example.com";

        assertTrue(email.length() > 255);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createUser(
                        "student1",
                        email,
                        "hashed-password",
                        User.UserType.STUDENT
                )
        );
    }

    @Test
    void createUserRejectsNullPasswordHash() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createUser(
                        "student1",
                        "student1@example.com",
                        null,
                        User.UserType.STUDENT
                )
        );
    }

    @Test
    void createUserRejectsBlankPasswordHash() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createUser(
                        "student1",
                        "student1@example.com",
                        "   ",
                        User.UserType.STUDENT
                )
        );
    }

    @Test
    void createUserRejectsNullUserType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createUser(
                        "student1",
                        "student1@example.com",
                        "hashed-password",
                        null
                )
        );
    }

    @Test
    void newUserDoesNotHaveDatabaseGeneratedValues() {
        User user = service.createUser(
                "student1",
                "student1@example.com",
                "hashed-password",
                User.UserType.STUDENT
        );

        assertNull(user.getUserId());
        assertNull(user.getCreatedAt());
    }
}