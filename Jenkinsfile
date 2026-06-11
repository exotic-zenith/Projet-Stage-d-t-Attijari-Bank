pipeline {
    agent any

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

        //stage('Build Frontend') {
        //    steps {
        //        dir('frontend') {
        //            sh 'npm install'
        //            sh 'npm run build'
        //        }
        //    }
        //}
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