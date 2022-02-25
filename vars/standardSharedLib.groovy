def call(){
    def FAILED_STAGE
    def utils = new org.shared.utils.Checkout(this)
    pipeline {
        agent {
            kubernetes {
            yamlFile 'build-pod.yaml'
            }

        }
        options { buildDiscarder(logRotator(numToKeepStr: '1')) }
        environment {
            MAVEN_OPTS = "-Dmaven.repo.local=/m2"
            DOCKERHUB_CREDENTIALS=credentials('dockerCredentials')
            ARGOCDIP=credentials('argocdip')
        }
        stages {
            stage('Checkout SMC') {
                
                utils.checkout 'https://github.com/3illH/gs-rest-service-pipeline'
            }
            stage('Build') {
                
            }
            stage('Build with Docker') {
                
            }
        }
    }
}