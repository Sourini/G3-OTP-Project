CREATE DATABASE IF NOT EXISTS flashcard_app
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE flashcard_app;

-- USERS
CREATE TABLE users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (set_id) REFERENCES flashcard_sets(set_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);