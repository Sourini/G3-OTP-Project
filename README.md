## Simply Flashcards

### 1. Application Overview

Simply Flashcards is a Java-based application developed for the Software Engineering Project TX00EY27-3013 course.

Students can create flashcard sets, add individual flashcards, and study them using the application. Users can register and log in to manage their own accounts securely.

---

#### Problem Summary

There’s a need for efficient, easy-to-use software to help with studying. Traditional
flashcards are a great way to study but can easily become a hassle to manage.

The goal of the project is to create an application that lets users easily create, study, share, and modify flashcards and also track their progress and stats. The application will use a
database to store users, flashcards, and stats.

---

#### Intended Users

The application has two primary users:

- Students - create, manage, and study their own flashcards.
- Teachers - create and manage their cards, and share card sets and quizzes

---

#### Features

- User registration and login.
- Create flashcard sets.
- Add flashcards to sets.
- View personal flashcard sets.
- Study flashcards.
- Show answers and move between cards.
- Database storage for users, flashcard sets, and flashcards.
- Responsive desktop UI.

---

#### Technologies Used

*Development*

- Java
- JavaFX
- Maven
- IntelliJ IDEA

*Database*

- MariaDB
- HeidiSQL

*Security*

- Bcrypt for password hashing

*DevOps and Tools*

- Jenkins
- Docker
- GitHub
- Trello

---

### 2. Product Vision

#### Vision statement

Our vision is to create a simple and user-friendly flashcard application that allows for effective and organized studying. The application allows students to create, manage, and study their own flashcard sets.

The application will use a JavaFX desktop interface and a MariaDB database to provide persistent storage for user accounts, flashcards, flashcard sets, quizzes, and learning statistics.

---

#### Project Objectives

- Develop a functional desktop application with an easy-to-use GUI.
- Implement flashcard management for creating, managing, and deleting cards and card sets.
- Implement progress tracking that users can review.
- Provide secure registration and login for users.
- Implement persistent data storage using MariaDB.

---

#### Future Development

- Separate teacher and student accounts
- Quiz functionality and reviewing progress
- Modifying and deleting cards
- Sharing card sets

---

### 3. Running the Project

#### Prerequisites for running locallly

- Java JDK 21+
- Maven
- MariaDB

#### Run Locally

1. Clone the repository.
2. Set up the MariaDB database using the provided database script located in `src/main/java/otpproju/flashcard_app_schema.sql`.
3. Configure the database connection in the project. Rename the provided .env.example to .env and edit your mariadb credentials in it.
4. Open the project root in console and build the project using Maven: `mvn clean install`
5. Run the JavaFX application: `mvn javafx:run`
6. The application will launch and can be used through the JavaFX interface.

---


#### Additional prerequisites for running the up to date docker image

- Docker
- Xming
- Local Java and MariaDB installations are only needed for running outside Docker.

#### Running the docker image

1. Clone the repository
2. Rename the provided .env.example file to .env and edit your with your credentials
3. Open the project root in console
4. run the following commands:
```
docker compose pull app
docker compose up -d --no-build
```
5. To stop, run
`docker compose down`
---
### 4. Testing Instructions

Testing is done using JUnit and Maven, but also require MariaDB for database integration.
The project is designed to be run with Jenkins for automated tests and docker image builds.

#### Additional steps to setup Jenkins

- maven is called using the name `maven3`
- mariadb credentials should be saved under ID `flashcard-db`
- dockerhub credentials should be saved under ID `dockerhub`
- Required plugins: `HTML Publisher plugin`, `Docker Pipeline`, `Jacoco`
- Jenkins polls the repo every 5 minutes for updates.
#### Run tests locally

From the project root directory, run: `mvn test`. This runs the available unit tests for the application.

#### Full verification

To build the project and run the tests, use: `mvn clean verify`

The tests cover important functionalities:

- User registration and login
- Password security
- Database operations
- Flashcard creation and management
- User data separation
- Application functionality
- Input validation

---

### 5. Project structure

#### Directories

- /src/main/java/otpproju/model - Data models such as users, flashcards, and flashcard sets
- /src/main/java/otpproju/repository - Database operations and repository classes
- /src/main/java/otpproju/screens - JavaFX user interface screens
- /src/main/java/otpproju/service - Business logic and services
- /src/test - Test cases
- /Documents - Project documentation
- /Diagrams - UML diagrams

---

### 6. Authors

#### Team members:

- Frans Rastas - Backend dev, Testing, Maven, Jenkins, Docker, Documentation, Troubleshooting
- Juli Javanainen - Database creation and development, SQL query writing, DB schema and ER design, Jenkinsfile
- Tuomas Kolari - Database development, Document creation and updation, Docker, Diagrams
- Olivia Toratti - Frontend Development, JavaFX GUI Development, UI Design, Application Testing
