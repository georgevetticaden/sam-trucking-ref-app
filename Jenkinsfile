pipeline {
    agent {
        docker {
            image 'maven:3-alpine' 
            args '-v /root/.m2:/root/.m2' 
        }
    }
    stages {
        stage('Build') { 
            steps {
                sh 'mvn -B -DskipTests clean package' 
            }
        }
        stage('Unit Test') {
            steps {
                sh 'mvn test'
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
            post {
                always {
                  junit 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('Deploy to SAM') {
            steps {
                sh 'chmod a+x jenkins/scripts/deploy-to-sam.sh'
                sh './jenkins/scripts/deploy-to-sam.sh' 
            }
           
        }

    }
}