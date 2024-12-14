# GitLab CI/CD Pipeline for Helm Chart Deployment

This repository contains a GitLab CI/CD pipeline designed to package and deploy Helm charts to a Google Cloud Platform (GCP) OCI-based Helm repository.

---

## Pipeline Overview

The pipeline automates the following steps:
1. Linting Helm charts to ensure correctness.
2. Generating templates for validation.
3. Packaging the Helm chart into a distributable `.tgz` file.
4. Updating the Helm repository index.
5. Logging into the GCP Artifact Registry using a service account key.
6. Pushing the Helm chart package to the specified OCI-based Helm repository.

---

## Variables

### Required Variables
The following variables need to be defined in your GitLab CI/CD settings:

| Variable Name        | Description                                                    |
|----------------------|----------------------------------------------------------------|
| `DOCKER_HOST`        | Docker host configuration (set to `tcp://docker:2376`).        |
| `DOCKER_TLS_CERTDIR` | Directory for Docker TLS certificates (e.g., `/certs`).        |
| `DOCKER_TLS_VERIFY`  | Enable TLS verification for Docker (`1` to enable).            |
| `DOCKER_CERT_PATH`   | Path to Docker client certificates (`$DOCKER_TLS_CERTDIR/client`). |
| `HELM_TAG`           | Version tag for the Helm chart (e.g., `${CI_COMMIT_SHORT_SHA}`). |
| `GCP_SA_HELM_KEY`    | Service account key for GCP Artifact Registry authentication.  |

---

## Pipeline Stages

### 1. **Lint Helm Charts**
The `helm lint` command checks the syntax and correctness of the Helm chart:
```yaml
helm lint ./helm-websocket

---------

# Helm Chart for Tech Backend Application

This Helm chart is used to deploy a backend application on Kubernetes with configurations for replicas, resources, Secrets, and ConfigMaps.

---

## Deployment Instructions

### Prerequisites
1. Ensure you have a running Kubernetes cluster.
2. Install Helm CLI. [Helm Installation Guide](https://helm.sh/docs/intro/install/)

### Steps to Deploy
1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd <repository-directory>

helm upgrade --install tech-backend ./chart-directory \
  --namespace <namespace> \
  --create-namespace \
  -f values.yaml

## change value
repository: nginx --> your repository
tag: "" --> your tag