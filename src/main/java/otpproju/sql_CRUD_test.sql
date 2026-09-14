USE flashcard_app;

START TRANSACTION;

SET @test_username = CONCAT('crud_test_', UUID_SHORT());

INSERT INTO users (username, password, usertype)
VALUES (@test_username, 'test_password', 'student');

SET @test_user_id = LAST_INSERT_ID();

SELECT
    'User CREATE' AS test_name,
    IF(
            EXISTS (
                SELECT 1
                FROM users
                WHERE user_id = @test_user_id
            ),
            'PASS',
            'FAIL'
    ) AS result;


-- READ USER
SELECT
    'User READ' AS test_name,
    IF(
            EXISTS (
                SELECT 1
                FROM users
                WHERE user_id = @test_user_id
                  AND username = @test_username
                  AND password = 'test_password'
                  AND usertype = 'student'
            ),
            'PASS',
            'FAIL'
    ) AS result;


-- UPDATE USER
SET @updated_username =
    CONCAT('updated_crud_test_', UUID_SHORT());

UPDATE users
SET username = @updated_username,
    usertype = 'teacher'
WHERE user_id = @test_user_id;

SELECT
    'User UPDATE' AS test_name,
    IF(
            EXISTS (
                SELECT 1
                FROM users
                WHERE user_id = @test_user_id
                  AND username = @updated_username
                  AND usertype = 'teacher'
            ),
            'PASS',
            'FAIL'
    ) AS result;


-- CREATE FLASHCARD SET
INSERT INTO flashcard_sets (
    user_id,
    title,
    description
)
VALUES (
           @test_user_id,
           'CRUD Test Set',
           'A temporary set used for database testing'
       );

SET @test_set_id = LAST_INSERT_ID();

SELECT
    'Flashcard set CREATE' AS test_name,
    IF(
            EXISTS (
                SELECT 1
                FROM flashcard_sets
                WHERE set_id = @test_set_id
            ),
            'PASS',
            'FAIL'
    ) AS result;


-- READ FLASHCARD SET
SELECT
    'Flashcard set READ' AS test_name,
    IF(
            EXISTS (
                SELECT 1
                FROM flashcard_sets
                WHERE set_id = @test_set_id
                  AND user_id = @test_user_id
                  AND title = 'CRUD Test Set'
                  AND description =
                      'A temporary set used for database testing'
            ),
            'PASS',
            'FAIL'
    ) AS result;


-- UPDATE FLASHCARD SET
UPDATE flashcard_sets
SET title = 'Updated CRUD Test Set',
    description = 'The set was updated successfully'
WHERE set_id = @test_set_id;

SELECT
    'Flashcard set UPDATE' AS test_name,
    IF(
            EXISTS (
                SELECT 1
                FROM flashcard_sets
                WHERE set_id = @test_set_id
                  AND title = 'Updated CRUD Test Set'
                  AND description =
                      'The set was updated successfully'
            ),
            'PASS',
            'FAIL'
    ) AS result;

-- CREATE FLASHCARD
INSERT INTO flashcards (
    set_id,
    question,
    answer
)
VALUES (
           @test_set_id,
           'What does JVM stand for?',
           'Java Virtual Machine'
       );

SET @test_card_id = LAST_INSERT_ID();

SELECT
    'Flashcard CREATE' AS test_name,
    IF(
            EXISTS (
                SELECT 1
                FROM flashcards
                WHERE card_id = @test_card_id
            ),
            'PASS',
            'FAIL'
    ) AS result;


-- READ FLASHCARD
SELECT
    'Flashcard READ' AS test_name,
    IF(
            EXISTS (
                SELECT 1
                FROM flashcards
                WHERE card_id = @test_card_id
                  AND set_id = @test_set_id
                  AND question = 'What does JVM stand for?'
                  AND answer = 'Java Virtual Machine'
            ),
            'PASS',
            'FAIL'
    ) AS result;


-- UPDATE FLASHCARD
UPDATE flashcards
SET question = 'What is the JVM?',
    answer = 'The Java Virtual Machine'
WHERE card_id = @test_card_id;

SELECT
    'Flashcard UPDATE' AS test_name,
    IF(
            EXISTS (
                SELECT 1
                FROM flashcards
                WHERE card_id = @test_card_id
                  AND question = 'What is the JVM?'
                  AND answer = 'The Java Virtual Machine'
            ),
            'PASS',
            'FAIL'
    ) AS result;


-- DELETE FLASHCARD
DELETE FROM flashcards
WHERE card_id = @test_card_id;

SELECT
    'Flashcard DELETE' AS test_name,
    IF(
            NOT EXISTS (
                SELECT 1
                FROM flashcards
                WHERE card_id = @test_card_id
            ),
            'PASS',
            'FAIL'
    ) AS result;

-- ON DELETE CASCADE.
INSERT INTO flashcards (
    set_id,
    question,
    answer
)
VALUES (
           @test_set_id,
           'Which keyword creates a Java class?',
           'class'
       );

SET @cascade_card_id = LAST_INSERT_ID();

DELETE FROM flashcard_sets
WHERE set_id = @test_set_id;

SELECT
    'Flashcard set DELETE' AS test_name,
    IF(
            NOT EXISTS (
                SELECT 1
                FROM flashcard_sets
                WHERE set_id = @test_set_id
            ),
            'PASS',
            'FAIL'
    ) AS result;

SELECT
    'Set-to-card cascade DELETE' AS test_name,
    IF(
            NOT EXISTS (
                SELECT 1
                FROM flashcards
                WHERE card_id = @cascade_card_id
            ),
            'PASS',
            'FAIL'
    ) AS result;

-- Return database to its original state.
ROLLBACK;

SELECT
    'Test transaction rolled back' AS information;