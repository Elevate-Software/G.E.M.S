//  CampusGate Jenkins Pipeline
pipeline {
    agent any

    environment {
        MVN         = './mvnw'
        WORK_DIR    = 'campusgate'
        JAVA_HOME   = tool 'JDK21'
        PATH        = "${env.JAVA_HOME}/bin:${env.PATH}"
        JWT_SECRET  = 'jenkins-ci-test-secret-key-for-campusgate-at-least-256-bits-xyz'
        JWT_EXPIRATION = '600000'
    }

    options {
        timeout(time: 20, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                echo "Building branch: ${env.BRANCH_NAME ?: 'local'}"
            }
        }

        stage('Build & Unit + Integration Tests') {
            steps {
                dir(WORK_DIR) {
                    sh "${MVN} clean verify \
                        -Dspring.datasource.url='jdbc:h2:mem:jenkins;DB_CLOSE_DELAY=-1;MODE=PostgreSQL' \
                        -Dspring.datasource.driver-class-name=org.h2.Driver \
                        -Dspring.datasource.username=sa \
                        -Dspring.datasource.password= \
                        -Dspring.jpa.hibernate.ddl-auto=create-drop \
                        -Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect \
                        -Dspring.flyway.enabled=false \
                        -Dspring.data.redis.host=localhost \
                        -Djwt.secret=${JWT_SECRET} \
                        -Djwt.expiration=${JWT_EXPIRATION}"
                }
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: "${WORK_DIR}/target/surefire-reports/*.xml"
                }
            }
        }

        stage('Publish Coverage Report') {
            steps {
                dir(WORK_DIR) {
                    echo "JaCoCo HTML report: ${WORK_DIR}/target/site/jacoco/index.html"
                }
            }
            post {
                always {
                    publishHTML(target: [
                        allowMissing          : true,
                        alwaysLinkToLastBuild : true,
                        keepAll               : true,
                        reportDir             : "${WORK_DIR}/target/site/jacoco",
                        reportFiles           : 'index.html',
                        reportName            : 'JaCoCo Coverage Report'
                    ])
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                dir(WORK_DIR) {
                    sh 'docker build -t campusgate:${BUILD_NUMBER} .'
                    echo "Docker image campusgate:${BUILD_NUMBER} built successfully"
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline PASSED; all tests green; coverage threshold met."
        }
        failure {
            echo "Pipeline FAILED; check the test report above for details."
        }
        always {
            archiveArtifacts artifacts: "${WORK_DIR}/target/site/jacoco/**",
                             allowEmptyArchive: true
        }
    }
}
