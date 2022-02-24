def call(){
    stage('Build with Docker') {
        steps {
            container('docker') {
                script{
                dockerImageName = "3ill/gs-rest-service:${pom.version}"
                dockerImage = docker.build("${dockerImageName}", ".")
                }
            }
        }
    }
}