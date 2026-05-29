pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    environment {
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
                withCredentials([
                    string(credentialsId: 'db.url', variable: 'DB_URL'),
                    string(credentialsId: 'db.username', variable: 'DB_USERNAME'),
                    string(credentialsId: 'db.password', variable: 'DB_PASSWORD'),
                    string(credentialsId: 'jwt.secret', variable: 'JWT_SECRET'),
                    string(credentialsId: 'google.client.id', variable: 'GOOGLE_CLIENT_ID'),
                    string(credentialsId: 'vision.api.key', variable: 'VISION_API_KEY'),
                    string(credentialsId: 'plantnet.api.key', variable: 'PLANTNET_API_KEY'),
                    string(credentialsId: 'admin.key', variable: 'ADMIN_KEY'),
                    string(credentialsId: 'GCS_BUCKET_NAME', variable: 'GCS_BUCKET_NAME'),
                    string(credentialsId: 'GCS_CREDENTIALS_B64', variable: 'GCS_CREDENTIALS_B64'),
                    string(credentialsId: 'GCP_PROJECT_ID', variable: 'GCP_PROJECT_ID'),
                    string(credentialsId: 'mail.host', variable: 'MAIL_HOST'),
                    string(credentialsId: 'mail.port', variable: 'MAIL_PORT'),
                    string(credentialsId: 'mail.username', variable: 'MAIL_USERNAME'),
                    string(credentialsId: 'mail.password', variable: 'MAIL_PASSWORD'),
                    string(credentialsId: 'mail.from', variable: 'MAIL_FROM')
                ]) {
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
                                  -DADMIN_KEY="$ADMIN_KEY" \
                                  -DMAIL_HOST="$MAIL_HOST" \
                                  -DMAIL_PORT="$MAIL_PORT" \
                                  -DMAIL_USERNAME="$MAIL_USERNAME" \
                                  -DMAIL_PASSWORD="$MAIL_PASSWORD" \
                                  -DMAIL_FROM="$MAIL_FROM" \
                                  -DSTORAGE_SERVICE_BASE_URL="https://group-6-15.pvt.dsv.su.se/storage-service" \
                                  -Dservices.auth.base-url="https://group-6-15.pvt.dsv.su.se/auth-service" \
                                  -Dservices.storage.base-url="https://group-6-15.pvt.dsv.su.se/storage-service" \
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
                                  -DADMIN_KEY=%ADMIN_KEY% ^
                                  -DMAIL_HOST=%MAIL_HOST% ^
                                  -DMAIL_PORT=%MAIL_PORT% ^
                                  -DMAIL_USERNAME=%MAIL_USERNAME% ^
                                  -DMAIL_PASSWORD=%MAIL_PASSWORD% ^
                                  -DMAIL_FROM=%MAIL_FROM% ^
                                  -DSTORAGE_SERVICE_BASE_URL="https://group-6-15.pvt.dsv.su.se/storage-service"
                            """
                        }
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
                    credentialsId: 'b538eb18-1f66-4a28-872c-9dbe9d6fe167',
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
    }
}