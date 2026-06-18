pipeline {
    agent any

    environment {
            HARBOR_URL = 'localhost:8082'
            HARBOR_PROJECT = 'library'
            APP_PORT = '8081'
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

        stage('Gate 1: Gitleaks Secrets Scan') {
            steps {
                sh 'gitleaks detect --source . --report-format json --report-path gitleaks-report.json --verbose || true'
            }
        }

        stage('Gate 2: Semgrep SAST Scan') {
            steps {
                sh 'semgrep --config=auto --json --output=semgrep-report.json . || true'
            }
        }

        stage('Gate 3: Trivy SCA Scan') {
            steps {
                sh 'trivy fs --scanners vuln --format json --output trivy-sca-report.json . || true'
            }
        }

        stage('Gate 4: Trivy Image Scan') {
            steps {
                sh 'trivy image --format json --output trivy-image-backend-report.json $HARBOR_URL/$HARBOR_PROJECT/account-service-backend:latest || true'
                sh 'trivy image --format json --output trivy-image-frontend-report.json $HARBOR_URL/$HARBOR_PROJECT/account-service-frontend:latest || true'
            }
        }

        stage('Gate 5: OWASP ZAP DAST Scan') {
            steps {
                script {
                    // Start the app with docker-compose
                    echo 'Starting application containers for ZAP scan...'
                    sh 'docker compose -f docker-compose.zap.yml up -d'

                    // Wait for backend to be ready
                    echo 'Waiting for backend to be reachable...'
                    sh '''
                        for i in $(seq 1 30); do
                            if curl -s http://172.17.0.1:8081:8081/api/accounts > /dev/null 2>&1; then
                                echo "Backend is up!"
                                break
                            fi
                            echo "Waiting... ($i/30)"
                            sleep 2
                        done
                    '''

                    // Debug: Verify app is running
                    sh 'echo "=== Running containers ===" && docker ps'
                    sh 'echo "=== Testing app endpoint ===" && curl -v http://172.17.0.1:8081/api/accounts || echo "APP NOT REACHABLE"'

                    // Create report directory with proper permissions
                    sh 'rm -rf zap-report && mkdir -p zap-report && chmod 777 zap-report'

                    // Run ZAP baseline scan
                    echo 'Running OWASP ZAP scan...'
                    sh '''
                        docker run --rm \
                            --network host \
                            -v $(pwd)/zap-report:/zap/wrk:rw \
                            --user $(id -u):$(id -g) \
                            ghcr.io/zaproxy/zaproxy:stable \
                            zap-baseline.py \
                            -t http://172.17.0.1:8081 \
                            -r zap-report.html \
                            -J zap-report.json \
                            -I
                    '''

                    // Stop the app
                    echo 'Stopping application containers...'
                    sh 'docker-compose down'
                }
            }
        }
    }



    post {
        always {
            archiveArtifacts artifacts: 'gitleaks-report.json, semgrep-report.json, trivy-sca-report.json, trivy-image-backend-report.json, trivy-image-frontend-report.json, zap-report.html, zap-report.json', allowEmptyArchive: true
            echo 'Pipeline finished'
        }
        failure {
            echo 'Build failed – check console output'
        }
    }
}
