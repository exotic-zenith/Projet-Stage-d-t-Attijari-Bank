pipeline {
    agent any

    environment {
            HARBOR_URL = 'localhost:8082'
            HARBOR_PROJECT = 'library'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Backend') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage ('Build and push Docker images to harbor') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'harbor-credentials', usernameVariable: 'HARBOR_USER', passwordVariable: 'HARBOR_PASS')]) {
                    sh '''
                        echo $HARBOR_PASS | docker login $HARBOR_URL -u $HARBOR_USER --password-stdin

                        docker build -t $HARBOR_URL/$HARBOR_PROJECT/account-service-backend:latest .
                        docker push $HARBOR_URL/$HARBOR_PROJECT/account-service-backend:latest

                        cd frontend
                        docker build -t $HARBOR_URL/$HARBOR_PROJECT/account-service-frontend:latest .
                        docker push $HARBOR_URL/$HARBOR_PROJECT/account-service-frontend:latest
                    '''
                }

        }

    }

    post {
        always {
            echo 'Pipeline finished'
        }
        failure {
            echo 'Build failed – check console output'
        }
    }
}