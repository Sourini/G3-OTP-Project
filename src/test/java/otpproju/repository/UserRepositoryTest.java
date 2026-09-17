package otpproju.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import otpproju.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class UserRepositoryTest {

    private UserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
    }

    @Test
    void performsCompleteUserCrudLifecycle() {
        String uniqueValue = UUID.randomUUID().toString();

        String username = "integration_" + uniqueValue;
        String email =
                "integration_" + uniqueValue + "@example.com";

        User user = new User(
                username,
                email,
                "initial-password-hash",
                User.UserType.STUDENT
        );

        Integer createdUserId = null;

        try {
            // CREATE
            User createdUser = repository.create(user);
            createdUserId = createdUser.getUserId();

            assertNotNull(createdUserId);
            assertEquals(username, createdUser.getUsername());
            assertEquals(email, createdUser.getEmail());
            assertEquals(
                    "initial-password-hash",
                    createdUser.getPasswordHash()
            );
            assertEquals(
                    User.UserType.STUDENT,
                    createdUser.getUserType()
            );
            assertNotNull(createdUser.getCreatedAt());

            // FIND BY ID
            Optional<User> foundById =
                    repository.findById(createdUserId);

            assertTrue(foundById.isPresent());
            assertEquals(createdUser, foundById.get());

            // FIND BY USERNAME
            Optional<User> foundByUsername =
                    repository.findByUsername(username);

            assertTrue(foundByUsername.isPresent());
            assertEquals(
                    createdUserId,
                    foundByUsername.get().getUserId()
            );

            // FIND BY EMAIL
            Optional<User> foundByEmail =
                    repository.findByEmail(email);

            assertTrue(foundByEmail.isPresent());
            assertEquals(
                    createdUserId,
                    foundByEmail.get().getUserId()
            );

            // FIND ALL
            List<User> users = repository.findAll();
            int expectedUserId = createdUserId;

            assertTrue(
                    users.stream().anyMatch(
                            listedUser ->
                                    listedUser.getUserId() != null
                                            && listedUser.getUserId()
                                            == expectedUserId
                    )
            );

            // UPDATE USER DETAILS
            String updatedUsername =
                    "updated_" + uniqueValue;

            String updatedEmail =
                    "updated_" + uniqueValue + "@example.com";

            createdUser.setUsername(updatedUsername);
            createdUser.setEmail(updatedEmail);
            createdUser.setUserType(User.UserType.TEACHER);

            assertTrue(repository.update(createdUser));

            User updatedUser = repository
                    .findById(createdUserId)
                    .orElseThrow();

            assertEquals(
                    updatedUsername,
                    updatedUser.getUsername()
            );
            assertEquals(
                    updatedEmail,
                    updatedUser.getEmail()
            );
            assertEquals(
                    User.UserType.TEACHER,
                    updatedUser.getUserType()
            );

            // UPDATE PASSWORD
            assertTrue(
                    repository.updatePasswordHash(
                            createdUserId,
                            "updated-password-hash"
                    )
            );

            User userWithUpdatedPassword = repository
                    .findById(createdUserId)
                    .orElseThrow();

            assertEquals(
                    "updated-password-hash",
                    userWithUpdatedPassword.getPasswordHash()
            );

            // DELETE
            assertTrue(
                    repository.deleteById(createdUserId)
            );

            assertTrue(
                    repository.findById(createdUserId).isEmpty()
            );

            createdUserId = null;

        } finally {
            // Clean up if an assertion fails before DELETE.
            if (createdUserId != null) {
                repository.deleteById(createdUserId);
            }
        }
    }

    @Test
    void returnsEmptyWhenUserDoesNotExist() {
        assertTrue(
                repository.findById(Integer.MAX_VALUE).isEmpty()
        );

        assertTrue(
                repository.findByUsername(
                        "missing_" + UUID.randomUUID()
                ).isEmpty()
        );

        assertTrue(
                repository.findByEmail(
                        UUID.randomUUID() + "@example.com"
                ).isEmpty()
        );
    }

    @Test
    void returnsFalseWhenUpdatingOrDeletingMissingUser() {
        User missingUser = new User(
                Integer.MAX_VALUE,
                "missing_user",
                "missing@example.com",
                "password-hash",
                User.UserType.STUDENT,
                null
        );

        assertFalse(repository.update(missingUser));

        assertFalse(
                repository.updatePasswordHash(
                        Integer.MAX_VALUE,
                        "new-password-hash"
                )
        );

        assertFalse(
                repository.deleteById(Integer.MAX_VALUE)
        );
    }
}