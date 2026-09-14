CREATE DATABASE flashcard_app;
USE flashcard_app;

-- USERS
CREATE TABLE users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    usertype    ENUM('student', 'admin', 'teacher') NOT NULL DEFAULT 'student',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- FLASHCARD SETS
CREATE TABLE flashcard_sets (
    set_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL,
    title       VARCHAR(100) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- FLASHCARDS
CREATE TABLE flashcards (
    card_id     INT AUTO_INCREMENT PRIMARY KEY,
    set_id      INT NOT NULL,
    question    TEXT NOT NULL,
    answer      TEXT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (set_id) REFERENCES flashcard_sets(set_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- CRUD testaus
INSERT INTO users (username, password, usertype)
VALUES ('demo_user', 'hashed_password_here', 'student');

INSERT INTO flashcard_sets (user_id, title, description)
VALUES (1, 'Spanish Basics', 'Common Spanish vocabulary words');

INSERT INTO flashcards (set_id, question, answer)
VALUES
(1, 'How do you say "hello"?', 'Hola'),
(1, 'How do you say "goodbye"?', 'Adiós');
