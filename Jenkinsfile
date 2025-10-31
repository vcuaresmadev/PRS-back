pipeline {
    agent any

    tools {
        jdk 'Java17'
        maven 'M3'
    }

    environment {
        GITHUB_REPO = 'https://github.com/MariaLazaroVelarde/PRS-back.git'
        MAVEN_OPTS = '-Xmx1024m'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📦 Clonando repositorio...'
                git branch: 'main', url: "${GITHUB_REPO}"
            }
        }

        stage('Build') {
            steps {
                echo '⚙️ Compilando el proyecto...'
                sh 'mvn clean compile'
            }
        }

        stage('Unit Tests') {
            steps {
                echo '🧪 Ejecutando pruebas unitarias...'
                sh 'mvn test -Dspring.profiles.active=test'
            }
            post {
                always {
                    echo '📄 Publicando resultados de pruebas...'
                    junit 'target/surefire-reports/*.xml'

                    script {
                        if (fileExists('target/site/jacoco/jacoco.xml')) {
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: 'target/site/jacoco/jacoco.xml']],
                                sourceCodeRetention: 'EVERY_BUILD',
                                failNoReports: false
                            )
                        } else {
                            echo '⚠️ No se encontró el reporte de JaCoCo'
                        }
                    }
                }
            }
        }

        stage('Integration Tests') {
            steps {
                echo '🔗 Ejecutando pruebas de integración...'
                sh 'mvn test -Dtest=*IntegrationTest'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Code Analysis') {
            steps {
                echo '🔍 Analizando código con SonarCloud...'
                withCredentials([string(credentialsId: 'sonar', variable: 'SONAR_TOKEN')]) {
                    sh '''
                        mvn clean verify sonar:sonar \
                            -Dsonar.projectKey=PRS-Back \
                            -Dsonar.organization=marialazaro \
                            -Dsonar.host.url=https://sonarcloud.io \
                            -Dsonar.token=$SONAR_TOKEN
                    '''
                }
            }
        }
    }

    post {
        always {
            echo '🧹 Limpiando workspace...'
            cleanWs()
        }
        success {
            echo '✅ Pipeline ejecutado con éxito!'
            slackSend(
                channel: '#prs-revisión',
                color: 'good',
                message: """
                ✅ *BUILD EXITOSO*
                Proyecto: *${env.JOB_NAME}*
                Build: *#${env.BUILD_NUMBER}*
                Ver detalles: ${env.BUILD_URL}
                """
            )
        }
        failure {
            echo '❌ Pipeline falló!'
            slackSend(
                channel: '#prs-revisión',
                color: 'danger',
                message: """
                ❌ *BUILD FALLIDO*
                Proyecto: *${env.JOB_NAME}*
                Build: *#${env.BUILD_NUMBER}*
                Ver detalles: ${env.BUILD_URL}
                """
            )
        }
    }
}
