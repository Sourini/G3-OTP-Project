package otpproju.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void newUserConstructorStoresProvidedValues() {
        User user = new User(
                "student1",
                "hashed-password",
                User.UserType.STUDENT
        );

        assertEquals("student1", user.getUsername());
        assertEquals(
                "hashed-password",
                user.getPasswordHash()
        );
        assertEquals(
                User.UserType.STUDENT,
                user.getUserType()
        );
        assertNull(user.getUserId());
        assertNull(user.getCreatedAt());
    }

    @Test
    void completeConstructorStoresDatabaseValues() {
        LocalDateTime createdAt =
                LocalDateTime.of(2026, 9, 14, 12, 30);

        User user = new User(
                10,
                "teacher1",
                "hashed-password",
                User.UserType.TEACHER,
                createdAt
        );

        assertEquals(10, user.getUserId());
        assertEquals("teacher1", user.getUsername());
        assertEquals(
                User.UserType.TEACHER,
                user.getUserType()
        );
        assertEquals(createdAt, user.getCreatedAt());
    }

    @Test
    void usersWithSameDatabaseIdAreEqual() {
        User first = new User();
        first.setUserId(10);

        User second = new User();
        second.setUserId(10);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void usersWithDifferentDatabaseIdsAreNotEqual() {
        User first = new User();
        first.setUserId(10);

        User second = new User();
        second.setUserId(20);

        assertNotEquals(first, second);
    }

    @Test
    void unsavedUsersAreNotEqual() {
        User first = new User();
        User second = new User();

        assertNotEquals(first, second);
    }

    @Test
    void userTypeConvertsFromDatabaseValue() {
        assertEquals(
                User.UserType.STUDENT,
                User.UserType.fromDatabaseValue("student")
        );

        assertEquals(
                User.UserType.TEACHER,
                User.UserType.fromDatabaseValue("teacher")
        );

        assertEquals(
                User.UserType.ADMIN,
                User.UserType.fromDatabaseValue("admin")
        );
    }

    @Test
    void userTypeConversionIgnoresCaseAndWhitespace() {
        assertEquals(
                User.UserType.TEACHER,
                User.UserType.fromDatabaseValue("  Teacher  ")
        );
    }

    @Test
    void userTypeConvertsToDatabaseValue() {
        assertEquals(
                "student",
                User.UserType.STUDENT.toDatabaseValue()
        );

        assertEquals(
                "teacher",
                User.UserType.TEACHER.toDatabaseValue()
        );
    }

    @Test
    void userTypeRejectsNullDatabaseValue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.UserType.fromDatabaseValue(null)
        );
    }

    @Test
    void userTypeRejectsUnknownDatabaseValue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.UserType.fromDatabaseValue("manager")
        );
    }

    @Test
    void toStringDoesNotRevealPasswordHash() {
        User user = new User(
                "student1",
                "secret-hash-value",
                User.UserType.STUDENT
        );

        String result = user.toString();

        assertTrue(result.contains("student1"));
        assertFalse(result.contains("secret-hash-value"));
    }
}