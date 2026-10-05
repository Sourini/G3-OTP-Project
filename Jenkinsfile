pipeline {
    agent any
    tools {
        maven 'maven3'
    }

    environment {
        DB_URL = 'jdbc:mariadb://127.0.0.1:3306/flashcard_app'
        DB_ACCOUNT = credentials('flashcard-db')
        DB_USER = "${DB_ACCOUNT_USR}"
        DB_PASSWORD = "${DB_ACCOUNT_PSW}"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/Sourini/G3-OTP-Project'
            }
        }
        stage('Build') {
            steps {
                bat 'mvn clean install'
            }
        }
        stage('Test') {
            steps {
                bat 'mvn test'
            }
        }
        stage('Code Coverage') {
            steps {
                bat 'mvn jacoco:report'
            }
        }
        stage('Publish Test Results') {
            steps {
                junit '**/target/surefire-reports/*.xml'
            }
        }
        stage('Publish Coverage Report') {
            steps {
                jacoco()
            }
        }
    }
}