def call(){
    def FAILED_STAGE
    pipeline {
        agent {
            kubernetes {
            yamlFile 'build-pod.yaml'
            }

        }
        environment {
            MAVEN_OPTS = "-Dmaven.repo.local=/m2"
            DOCKERHUB_CREDENTIALS=credentials('dockerCredentials')
            ARGOCDIP=credentials('argocdip')
        }
        stages {
            stage('Checkout SMC') {
                def utils = new org.shared.utils.Checkout(this)
                utils.checkout 'https://github.com/3illH/gs-rest-service-pipeline'
            }
            stage('Build') {
                maven
            }
            stage('Build with Docker') {
                dockerBuild
            }
        }
    }
}