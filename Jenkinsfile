pipeline {
    agent {
        docker {
            image 'maven:3-alpine' 
            args '-v /root/.m2:/root/.m2' 
        }
    }
    stages {
        stage('Build Trucking Ref App') { 
            steps {
                sh 'mvn -B -DskipTests clean package' 
            }
        }
        stage('Execute SAM Test Cases using Junit') {
            steps {
                sh 'mvn clean -Dtest=TruckingRefAdvancedAppTest test'
            }
            post {
                always {
                  junit 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('Package and Install') {
            steps {
                sh 'mvn clean package install  -DskipTests=true'
            }
        }
        stage('Deploy Trucking Ref App to SAM') {
            steps {
                sh 'chmod a+x jenkins/scripts/deploy-to-sam.sh'
                sh './jenkins/scripts/deploy-to-sam.sh' 
            }
           
        }

    }
}