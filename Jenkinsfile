// HealthNet Jenkins CI/CD Pipeline
// Stages: Checkout → Build → Test → Security Scan → Docker Build → Push → Deploy

pipeline {
    agent {
        docker {
            image 'maven:3.9.6-eclipse-temurin-17'
            args '-v /var/run/docker.sock:/var/run/docker.sock -v $HOME/.m2:/root/.m2'
        }
    }

    environment {
        DOCKER_REGISTRY     = credentials('docker-registry-credentials')
        DOCKER_IMAGE_PREFIX = 'healthnet'
        K8S_KUBECONFIG      = credentials('k8s-kubeconfig')
        SONAR_TOKEN         = credentials('sonarqube-token')
        DOCKER_BUILDKIT     = '1'
        // NVD API key for OWASP dependency check (get free key at nvd.nist.gov)
        NVD_API_KEY         = credentials('nvd-api-key')
    }

    options {
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    env.BUILD_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT_SHORT}"
                    echo "Building tag: ${env.BUILD_TAG}"
                }
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile -q --no-transfer-progress'
            }
        }

        stage('Unit Tests') {
            steps {
                sh 'mvn test -q --no-transfer-progress'
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                    publishCoverage adapters: [jacocoAdapter('**/target/site/jacoco/jacoco.xml')]
                }
            }
        }

        stage('Integration Tests') {
            steps {
                // Testcontainers requires Docker socket access
                sh 'mvn verify -Pintegration-test -q --no-transfer-progress'
            }
            post {
                always {
                    junit testResults: '**/target/failsafe-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Code Coverage Check') {
            steps {
                sh '''
                    mvn jacoco:check --no-transfer-progress
                    echo "Code coverage check passed (minimum 80%)"
                '''
            }
        }

        stage('OWASP Dependency Check') {
            steps {
                echo 'Running OWASP Dependency Check for known CVEs in dependencies...'
                sh '''
                    mvn org.owasp:dependency-check-maven:check \
                        -DnvdApiKey=${NVD_API_KEY} \
                        -DfailBuildOnCVSS=7 \
                        -Dformat=HTML \
                        -DsuppressionFile=owasp-suppressions.xml \
                        --no-transfer-progress || true
                '''
            }
            post {
                always {
                    publishHTML(target: [
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target',
                        reportFiles: 'dependency-check-report.html',
                        reportName: 'OWASP Dependency Check Report'
                    ])
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests -q --no-transfer-progress'
                archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
            }
        }

        stage('Docker Build & Push') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                script {
                    def services = [
                        'api-gateway',
                        'patient-identity-service',
                        'emr-service',
                        'appointment-service',
                        'telemedicine-service',
                        'pharmacy-service',
                        'analytics-service'
                    ]

                    docker.withRegistry("https://${DOCKER_REGISTRY_USR}", 'docker-registry-credentials') {
                        services.each { svc ->
                            echo "Building Docker image for ${svc}..."
                            def img = docker.build(
                                "${DOCKER_IMAGE_PREFIX}/${svc}:${BUILD_TAG}",
                                "--file ${svc}/Dockerfile ${svc}"
                            )
                            img.push()
                            img.push('latest')
                            echo "Pushed ${DOCKER_IMAGE_PREFIX}/${svc}:${BUILD_TAG}"
                        }
                    }
                }
            }
        }

        stage('Deploy to Dev') {
            when { branch 'develop' }
            steps {
                echo 'Deploying to development environment...'
                withKubeConfig([credentialsId: 'k8s-kubeconfig']) {
                    sh """
                        cd infrastructure/kubernetes/overlays/dev
                        kustomize edit set image \\
                            healthnet/api-gateway=${DOCKER_IMAGE_PREFIX}/api-gateway:${BUILD_TAG} \\
                            healthnet/patient-identity-service=${DOCKER_IMAGE_PREFIX}/patient-identity-service:${BUILD_TAG}
                        kubectl apply -k .
                        kubectl rollout status deployment/api-gateway -n healthnet --timeout=120s
                        kubectl rollout status deployment/patient-identity-service -n healthnet --timeout=120s
                    """
                }
            }
        }

        stage('Deploy to Production') {
            when { branch 'main' }
            input {
                message "Deploy to PRODUCTION?"
                ok "Yes, deploy it!"
                submitter "admin,scrum-master"
            }
            steps {
                echo 'Deploying to production with rolling update...'
                withKubeConfig([credentialsId: 'k8s-kubeconfig']) {
                    sh """
                        cd infrastructure/kubernetes/overlays/prod
                        # Update all image tags
                        kustomize edit set image \\
                            healthnet/api-gateway=${DOCKER_IMAGE_PREFIX}/api-gateway:${BUILD_TAG} \\
                            healthnet/patient-identity-service=${DOCKER_IMAGE_PREFIX}/patient-identity-service:${BUILD_TAG} \\
                            healthnet/emr-service=${DOCKER_IMAGE_PREFIX}/emr-service:${BUILD_TAG} \\
                            healthnet/appointment-service=${DOCKER_IMAGE_PREFIX}/appointment-service:${BUILD_TAG} \\
                            healthnet/telemedicine-service=${DOCKER_IMAGE_PREFIX}/telemedicine-service:${BUILD_TAG} \\
                            healthnet/pharmacy-service=${DOCKER_IMAGE_PREFIX}/pharmacy-service:${BUILD_TAG} \\
                            healthnet/analytics-service=${DOCKER_IMAGE_PREFIX}/analytics-service:${BUILD_TAG}

                        kubectl apply -k .

                        # Wait for all rolling updates to complete
                        for svc in api-gateway patient-identity-service emr-service appointment-service telemedicine-service pharmacy-service analytics-service; do
                            echo "Waiting for \$svc rollout..."
                            kubectl rollout status deployment/\$svc -n healthnet --timeout=180s
                        done

                        echo "All services deployed successfully."
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline succeeded for build ${env.BUILD_TAG}"
        }
        failure {
            echo "Pipeline FAILED for build ${env.BUILD_TAG}. Check logs above."
        }
        always {
            cleanWs()
        }
    }
}
