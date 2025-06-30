#!/bin/bash
set -e

echo "🚀 Starting CI/CD Pipeline..."

# Test
echo "📋 Running tests..."
./mvnw test

# Build
echo "🔨 Building application..."
./mvnw clean package -DskipTests

# Setup Minikube Docker environment
echo "🐳 Setting up Docker environment..."
eval $(minikube docker-env)

# Build Docker image with timestamp tag
TAG=$(date +%Y%m%d-%H%M%S)
echo "🏗️ Building Docker image with tag: $TAG"
docker build -t rabbitmq-consumer:$TAG .
docker tag rabbitmq-consumer:$TAG rabbitmq-consumer:latest

# Deploy RabbitMQ if not exists
echo "🐰 Deploying RabbitMQ..."
kubectl apply -f rabbitmq-deployment.yaml

# Wait for RabbitMQ
echo "⏳ Waiting for RabbitMQ..."
kubectl wait --for=condition=available --timeout=300s deployment/rabbitmq

# Deploy application
echo "🚀 Deploying application..."
sed "s|rabbitmq-consumer:latest|rabbitmq-consumer:$TAG|g" k8s-deployment.yaml | kubectl apply -f -

# Verify deployment
echo "✅ Verifying deployment..."
kubectl get pods
kubectl logs -l app=rabbitmq-consumer --tail=20

echo "🎉 Deployment complete!"