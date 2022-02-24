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
            stage{
                checkout
            }
            stage{
                maven
            }
            stage{
                dockerBuild
            }
        }
    }
}