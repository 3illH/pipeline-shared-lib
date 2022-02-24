def call() {
    stage('Build') {
        steps {
            container('maven') {
                script {
                    pom = readMavenPom file: "pom.xml";
                    sh "mvn clean package -DskipTests"
                }
            }
        }
    }
}