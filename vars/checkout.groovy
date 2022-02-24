def call(){
    stage('Checkout SMC') {
        steps {
            checkout([$class: 'GitSCM', branches: [[name: 'master']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/3illH/gs-rest-service-pipeline']]])
        }
    }
}