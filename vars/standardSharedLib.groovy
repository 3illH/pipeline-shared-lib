def call(Map config){
    def FAILED_STAGE
    pipeline {

    agent {
        kubernetes {
            yamlFile 'build-pod.yaml'
        }
    }
    environment {
        MAVEN_OPTS = "-Dmaven.repo.local=/m2"
        // DOCKERHUB_CREDENTIALS=credentials('dockerCredentials')
        HARBOR_CREDENTIALS=credentials('harborCredentials')
        // ARGOCDIP=credentials('argocdip')
        WEBHOOK = credentials('jenkins-webhook')
    }
    options {
        office365ConnectorWebhooks([
            [name: "jenkins-webhook", url: '$WEBHOOK', notifyBackToNormal: true, notifyFailure: true, notifyRepeatedFailure: true, notifySuccess: true, notifyAborted: true]
        ])
    }
    stages {
        stage('Checkout') {
            steps{
                checkoutStage(config)
            }
        }

        stage('Build') {
            when { expression { return config.steps.contains("build") } }
            steps {
                container('maven') {
                    script {
                        pom = readMavenPom file: "pom.xml";
                        sh "mvn clean package -DskipTests"
                    }
                }
            }
        }

        // stage('Test') {
        //     when { expression { return config.steps.contains("test") } }
        //     steps {
        //         container('maven') {
        //             script {
        //                 sh "mvn clean verify"
        //                 dependencyCheckPublisher pattern: 'target/dependency-check-report.xml'
        //             }
        //         }
        //     }
        // }

        // if(config.steps.contains("sonar")){
        //     stage('SonarQube Analysis') {
        //         steps {
        //             container('maven') {
        //                 withSonarQubeEnv('Sonar') {
        //                     sh "mvn clean verify sonar:sonar -Dsonar.projectKey=gs-rest-service"
        //                 }
        //             }
        //         }
        //     }
        // }

        stage('Build with Docker') {
            when { expression { return config.steps.contains("dockerBuild") } }
            steps {
                container('docker') {
                    script{
                        dockerImageName = "harbor-portal.harbor.svc.cluster.local/harbor/${config.projectName}:${pom.version}"
                        dockerImage = docker.build("${dockerImageName}", ".")
                    }
                }
            }
        }


        // stage('Trivy Scan Container image') {
        //     when { expression { return config.steps.contains("trivy") } }
        //     steps {
        //         container('trivy') {
        //             script {
        //                 FAILED_STAGE=env.STAGE_NAME
        //                 sh "trivy image -f json -o trivy-results.json harbor-portal.harbor.svc.cluster.local/harbor/${config.projectName}:${pom.version}"
        //             }
        //         }
        //     }
        // }

        // if(config.steps.contains("dockerPush")){
        //     stage('Push with Docker') {
        //         steps {
        //             container('docker') {
        //                 script{
        //                     sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
        //                     sh "docker push ${dockerImageName}"
        //                 }
        //             }
        //         }
        //     }
        // }


        stage('Push with Docker to Harbor') {
            when { expression { return config.steps.contains("dockerPush") } }
            steps {
                container('docker') {
                    withCredentials([usernamePassword(credentialsId: 'harborCredentials', passwordVariable: 'harborPSW', usernameVariable: 'harborUser')]) {
                        script{
                            sh 'docker login core.harbor.domain -u $harborUser --password $harborPSW'
                            sh "docker push ${dockerImageName}"
                        }
                    }
                }
            }
        }

        // if(config.steps.contains("argocd")){
        //     stage('Deploy with ArgoCd') {
        //         steps {
        //             container('argocd'){
        //                 withCredentials([usernamePassword(credentialsId: 'argocd', passwordVariable: 'argopassword', usernameVariable: 'argousername')]) {
        //                     sh "argocd login $ARGOCDIP --insecure --username=$argousername --password=$argopassword"
        //                     sh "argocd app set  gs-rest-service --kustomize-image ${dockerImageName}"
        //                     sh "argocd app sync gs-rest-service --force --prune"
        //                     sh "argocd app wait gs-rest-service --timeout 600"
        //                 }
        //             }
        //         }
        //     }
        // }
    }
    post {
        always {
            // recordIssues enabledForFailure: true, tool: trivy(pattern: 'trivy-results.json')
            // script{
            // def total = tm stringWithMacro: '${ANALYSIS_ISSUES_COUNT, tool="trivy", type="TOTAL"}'
            // def news = tm stringWithMacro: '${ANALYSIS_ISSUES_COUNT, tool="trivy", type="NEW"}'
            // def totalHight = tm stringWithMacro: '${ANALYSIS_ISSUES_COUNT, tool="trivy", type="TOTAL_HIGH"}'
            // def totalNormal = tm stringWithMacro: '${ANALYSIS_ISSUES_COUNT, tool="trivy", type="TOTAL_NORMAL"}'
            // echo "TOTAL: " + total
            // echo "NEWS: " + news
            // echo "HIGH: " + totalHight
            // echo "NORMAL: " + totalNormal
            // }
            // recordIssues enabledForFailure: true, tool: owaspDependencyCheck(pattern: 'target/dependency-check-report.json')
            script{
                def url = "https://www.google.es/"
                echo "Calling vJenkSYS to approve product on ${url}..."
                def headers = []
                def headerRow = [:]
                // headerRow.name = 'Authorization'
                // headerRow.value = credentials
                headers.add(headerRow)
                def response = httpRequest(url: url, httpMode: 'GET')
                if (response.getStatus() >= 400) {
                    echo response.getContent()
                    error("Error approving on vJenSYS. Response code: ${response.getStatus()}")
                } else {
                    echo 'Succesfully.'
                }

            }
        }
    }
    }
}