pipeline {
    agent any
    
    stages {
        stage('Test') {
            steps {
                sh './mvnw test'
            }
        }
        
        stage('Build') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    sh 'eval $(minikube docker-env) && docker build -t rabbitmq-consumer:${BUILD_NUMBER} .'
                }
            }
        }
        
        stage('Deploy') {
            steps {
                sh 'kubectl apply -f rabbitmq-deployment.yaml'
                sh 'IMAGE_TAG=${BUILD_NUMBER} envsubst < k8s-deployment.yaml | kubectl apply -f -'
            }
        }
        
        stage('Verify') {
            steps {
                sh 'kubectl get pods'
                sh 'kubectl logs -l app=rabbitmq-consumer --tail=50'
            }
        }
    }
}