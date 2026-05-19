pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    environment {
        // Secret text credentials in Jenkins
        DB_URL = credentials('DB_URL')
        DB_USERNAME = credentials('DB_USERNAME')
        DB_PASSWORD = credentials('DB_PASSWORD')
        JWT_SECRET = credentials('JWT_SECRET')
        GOOGLE_CLIENT_ID = credentials('GOOGLE_CLIENT_ID')
        VISION_API_KEY = credentials('VISION_API_KEY')
        PLANTNET_API_KEY = credentials('PLANTNET_API_KEY')
        GCS_BUCKET_NAME = credentials('GCS_BUCKET_NAME')
        GCP_PROJECT_ID = credentials('GCP_PROJECT_ID')
        GCS_CREDENTIALS_B64 = credentials('GCS_CREDENTIALS_B64')
        ADMIN_KEY = credentials('ADMIN_KEY')

        TOMCAT_MANAGER_URL = 'https://group-6-15.pvt.dsv.su.se/manager/text'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                script {
                    if (isUnix()) {
                        sh '''
                            chmod +x mvnw
                            ./mvnw clean verify \
                              -DDB_URL="$DB_URL" \
                              -DDB_USERNAME="$DB_USERNAME" \
                              -DDB_PASSWORD="$DB_PASSWORD" \
                              -DJWT_SECRET="$JWT_SECRET" \
                              -DGOOGLE_CLIENT_ID="$GOOGLE_CLIENT_ID" \
                              -DVISION_API_KEY="$VISION_API_KEY" \
                              -DPLANTNET_API_KEY="$PLANTNET_API_KEY" \
                              -DGCS_BUCKET_NAME="$GCS_BUCKET_NAME" \
                              -DGCP_PROJECT_ID="$GCP_PROJECT_ID" \
                              -DGCS_CREDENTIALS_B64="$GCS_CREDENTIALS_B64" \
                              -DADMIN_KEY="$ADMIN_KEY"
                        '''
                    } else {
                        bat """
                            mvnw.cmd clean verify ^
                              -DDB_URL=%DB_URL% ^
                              -DDB_USERNAME=%DB_USERNAME% ^
                              -DDB_PASSWORD=%DB_PASSWORD% ^
                              -DJWT_SECRET=%JWT_SECRET% ^
                              -DGOOGLE_CLIENT_ID=%GOOGLE_CLIENT_ID% ^
                              -DVISION_API_KEY=%VISION_API_KEY% ^
                              -DPLANTNET_API_KEY=%PLANTNET_API_KEY% ^
                              -DGCS_BUCKET_NAME=%GCS_BUCKET_NAME% ^
                              -DGCP_PROJECT_ID=%GCP_PROJECT_ID% ^
                              -DGCS_CREDENTIALS_B64=%GCS_CREDENTIALS_B64% ^
                              -DADMIN_KEY=%ADMIN_KEY%
                        """
                    }
                }
            }
        }

        stage('Archive WARs') {
            steps {
                archiveArtifacts artifacts: 'app-service/target/*.war,auth-service/target/*.war,storage-service/target/*.war', fingerprint: true
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'tomcat-deploy-credentials',
                    usernameVariable: 'TOMCAT_USER',
                    passwordVariable: 'TOMCAT_PASS'
                )]) {
                    script {
                        if (isUnix()) {
                            sh '''
                                curl --fail --user "$TOMCAT_USER:$TOMCAT_PASS" \
                                  --upload-file app-service/target/ROOT.war \
                                  "$TOMCAT_MANAGER_URL/deploy?path=/&update=true"

                                curl --fail --user "$TOMCAT_USER:$TOMCAT_PASS" \
                                  --upload-file auth-service/target/auth-service.war \
                                  "$TOMCAT_MANAGER_URL/deploy?path=/auth-service&update=true"

                                curl --fail --user "$TOMCAT_USER:$TOMCAT_PASS" \
                                  --upload-file storage-service/target/storage-service.war \
                                  "$TOMCAT_MANAGER_URL/deploy?path=/storage-service&update=true"
                            '''
                        } else {
                            bat """
                                curl.exe --fail --user %TOMCAT_USER%:%TOMCAT_PASS% --upload-file app-service/target/ROOT.war "%TOMCAT_MANAGER_URL%/deploy?path=/^&update=true"
                                curl.exe --fail --user %TOMCAT_USER%:%TOMCAT_PASS% --upload-file auth-service/target/auth-service.war "%TOMCAT_MANAGER_URL%/deploy?path=/auth-service^&update=true"
                                curl.exe --fail --user %TOMCAT_USER%:%TOMCAT_PASS% --upload-file storage-service/target/storage-service.war "%TOMCAT_MANAGER_URL%/deploy?path=/storage-service^&update=true"
                            """
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Build and deployment completed successfully.'
        }
        failure {
            echo 'Build or deployment failed.'
        }
        always {
            cleanWs()
        }
    }
}