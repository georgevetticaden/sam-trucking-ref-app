pipeline {
    agent {
        docker {
            image 'maven:3-alpine' 
            args '-v /root/.m2:/root/.m2' 
        }
    }
    stages {
        stage('Build SAM Trucking Reference Application') { 
            steps {
                sh 'mvn -B -DskipTests clean package' 
            }
        }
        stage('Unit Test Trucking Reference Application using SAM Test Cases') {
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
        stage('Deploy Truking Reference App to SAM') {
            steps {
                sh 'chmod a+x jenkins/scripts/deploy-to-sam.sh'
                sh './jenkins/scripts/deploy-to-sam.sh' 
            }
           
        }

    }
}