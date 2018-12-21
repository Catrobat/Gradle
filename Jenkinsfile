#!groovy

pipeline {
    agent {
        docker {
            image 'openjdk:8-jdk'
            args '-v /var/local/container_shared/gradle_cache/$EXECUTOR_NUMBER:/home/user/.gradle -m=6.5G'
        }
    }

    environment {
        GRADLE_USER_HOME = '/home/user/.gradle'
    }

    options {
        timeout(time: 2, unit: 'HOURS')
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '30'))
    }

    triggers {
        cron(env.BRANCH_NAME == 'master' ? '@midnight' : '')
        issueCommentTrigger('.*test this please.*')
    }

    stages {
        stage('Build and Test') {
            steps {
                sh './gradlew build'
                archiveArtifacts '**/build/libs/*.jar'
            }
        }
    }

    post {
        always {
            junit '**/*TEST*.xml'
        }
    }
}
