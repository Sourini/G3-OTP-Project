-- Sample-Data
INSERT INTO flashcard_sets (user_id, title, description)
SELECT
    user_id,
    'Spanish Basics',
    'Common Spanish vocabulary words'
FROM users
WHERE username = 'demo_user';

INSERT INTO flashcards (set_id, question, answer)
SELECT
    set_id,
    'How do you say "hello"?',
    'Hola'
FROM flashcard_sets
WHERE title = 'Spanish Basics'
ORDER BY set_id DESC
    LIMIT 1;

INSERT INTO flashcards (set_id, question, answer)
SELECT
    set_id,
    'How do you say "goodbye"?',
    'Adiós'
FROM flashcard_sets
WHERE title = 'Spanish Basics'
ORDER BY set_id DESC
    LIMIT 1;

-- CRUD tests
START TRANSACTION;

-- create
INSERT INTO users (username, password_hash, user_type)
VALUES ('crud_test', 'ultra_safe_hashed_password', 'student');

SET @test_user_id = LAST_INSERT_ID();

INSERT INTO flashcard_sets (user_id, title, description)
VALUES (@test_user_id, 'test title', 'test description');

SET @test_set_id = LAST_INSERT_ID();

INSERT INTO flashcards (set_id, question, answer)
VALUES (@test_set_id, 'question', 'answer');

SET @test_card_id = LAST_INSERT_ID();

-- read
SELECT *
FROM flashcards
WHERE card_id = @test_card_id;

-- update
UPDATE flashcards
SET question = 'Updated question',
    answer = 'Updated answer'
WHERE card_id = @test_card_id;

SELECT *
FROM flashcards
WHERE card_id = @test_card_id;

-- delete
DELETE FROM flashcards
WHERE card_id = @test_card_id;

SELECT *
FROM flashcards
WHERE card_id = @test_card_id;

-- rollback the crud test data
ROLLBACK;
