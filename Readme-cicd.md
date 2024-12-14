# Jenkins CI/CD Pipeline for Kubernetes Deployment

This Jenkins pipeline automates the CI/CD process for deploying a service to a Kubernetes environment. It handles tasks like Docker image building using Kaniko, Helm-based deployments, and environment configuration.

---

## Features

- **Automated CI/CD Workflow**:
  - Checkout and manage service configurations.
  - Build and push Docker images to AWS Elastic Container Registry (ECR) using Kaniko.
  - Deploy to Kubernetes using Helm charts.
- **Secure Secrets Management**:
  - Decrypt Helm chart secrets using SOPS and AWS IAM roles.
- **Notifications**:
  - Send deployment status notifications via Telegram.

---

## Prerequisites

1. **Jenkins Environment**:
   - Jenkins with Kubernetes plugin installed.
   - Access to a Kubernetes cluster for running build agents.

2. **Credentials**:
   - GitHub Personal Access Token (`github-token`).
   - AWS IAM role configured with permissions to manage ECR, S3, and STS.

3. **Dependencies**:
   - Helm CLI installed on Jenkins agents.
   - SOPS for secret decryption.
   - Telegram Bot token and Chat ID for notifications (if enabled).

4. **Repositories**:
   - Service Helm chart configurations in a repository.
   - Dockerfile present in the service's codebase.

---

## Pipeline Overview

### 1. **Agent Setup**
- The pipeline runs on a Kubernetes agent defined in the `yaml` block.
- Uses the Kaniko container for Docker image building.

### 2. **Pipeline Stages**

#### **Checkout**
Clones the Helm chart configuration repository for managing environment-specific values.

#### **Set Build Name**
Sets the Jenkins build name to the service version being deployed.

#### **Decrypt Helm Chart Secrets**
- Uses AWS IAM to assume a deployment role.
- Decrypts secrets required for Helm chart values using SOPS.

#### **Build and Push Docker Image**
- Builds the Docker image using Kaniko.
- Pushes the image to AWS ECR with the specified version tag.

#### **Deploy Helm Chart**
- Tags the image in ECR with the environment-specific label.
- Deploys or upgrades the service on Kubernetes using Helm.

### 3. **Post Actions**
- Sends notifications to Telegram for:
  - Successful deployments.
  - Failed deployments.
  - Aborted jobs.

---

## Configuration

### Environment Variables
Configure these variables in the Jenkins pipeline:
- **Project Details**:
  - `PROJECT_NAME`: Name of the project.
  - `SERVICE_NAME`: Name of the service.
  - `IMAGE_NAME`: Docker image name.
- **Repository and Deployment**:
  - `SERVICE_REPO_URL`: Repository URL of the service.
  - `ENVIRONMENT_NAME`: Deployment environment (e.g., staging, production).
  - `ECR_URL`: AWS Elastic Container Registry URL.
- **AWS Settings**:
  - `AWS_ACCOUNT_ID`: AWS account ID.
  - `AWS_REGION`: AWS region.
- **Helm Chart Details**:
  - `HELMCHART_OCI`: OCI URL for the Helm chart.
  - `HELMCHART_VERSION`: Helm chart version.
- **Notifications**:
  - `TELEGRAM_NOTIFICATION_ENABLED`: Enable Telegram notifications (`true`/`false`).
  - `TELEGRAM_CHAT_ID`: Telegram Chat ID for notifications.
  - `TELEGRAM_BOT_TOKEN`: Telegram bot token.

### Secrets Management
Ensure Helm chart secrets are encrypted using SOPS and placed in the correct directory structure within the Helm values repository.

---

## Example Usage

### Triggering the Pipeline
- Use the `VERSION` parameter to specify the service version during pipeline execution.
- The pipeline will automatically handle the deployment process.

### Telegram Notifications
- Set `TELEGRAM_NOTIFICATION_ENABLED` to `true` for receiving status updates.
- Ensure the bot token and chat ID are correctly configured.

---

## Troubleshooting

1. **Pipeline Fails During Secrets Decryption**:
   - Verify AWS IAM role permissions.
   - Check that the SOPS-encrypted secrets are properly configured.

2. **Docker Image Build Issues**:
   - Ensure the `Dockerfile` is present in the repository.
   - Confirm the Kaniko container has necessary permissions for pushing to ECR.

3. **Helm Deployment Errors**:
   - Validate the Helm chart values and version compatibility.
   - Ensure the Kubernetes namespace exists or use the `--create-namespace` flag.

---

