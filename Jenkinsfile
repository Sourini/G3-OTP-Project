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
        DOCKERHUB_REPO = 'sourini/g3-flashcard-app'
    }

    triggers {
        pollSCM('H/5 * * * *')
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

                publishHTML(target: [
                    reportDir: 'target/site/jacoco',
                    reportFiles: 'index.html',
                    reportName: 'JaCoCo HTML Report',
                    keepAll: true,
                    alwaysLinkToLastBuild: true,
                    allowMissing: false
                ])
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${env.DOCKERHUB_REPO}:${env.BUILD_NUMBER}")
                }
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                script {
                    docker.withRegistry(
                        'https://index.docker.io/v1/',
                        'dockerhub'
                    ) {
                        def image = docker.image(
                            "${env.DOCKERHUB_REPO}:${env.BUILD_NUMBER}"
                        )

                        image.push()
                        image.push('latest')
                    }
                }
            }
        }
    }
}