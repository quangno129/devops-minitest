Jenkins Deployment with Helmfile
This guide describes how to deploy Jenkins on Kubernetes using Helmfile, while keeping sensitive values (like passwords and tokens) secure with SOPS or Kubernetes Secrets.

1. Prerequisites
Ensure you have the following tools installed:

Helm: Kubernetes package manager

curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
Helmfile: Simplifies Helm chart deployments

brew install helmfile
SOPS: Securely encrypt/decrypt sensitive values

brew install sops
kubectl: Kubernetes CLI

brew install kubectl
2. Directory Structure
Organize your project as follows:

helmfile-jenkins/
│
├── helmfile.yaml         # Helmfile configuration
├── values.yaml           # Non-sensitive Jenkins configuration

3. Helmfile Configuration
helmfile.yaml
This Helmfile configures Jenkins to read values.yaml for non-sensitive configurations and secrets.yaml for sensitive values.

Apply Helmfile: Run the following command to deploy Jenkins:

helmfile apply


Jenkins Pipeline Configuration Guide
This document explains how to add new pipelines to your Jenkins setup by updating the NODEJS_BACKEND variable in the Constants class.

1. Overview
The NODEJS_BACKEND list in the Constants class defines the configuration for Node.js backend services. By adding a new service to this list, you automatically include it in the pipeline without modifying the main Jenkins pipeline script.

Each service requires the following parameters:

Parameter	Description	Example Value
name	Logical name of the service	buddy_Trading
image_name	Name of the Docker image to build	buddy_trading
repo_url	Git repository URL for the service	https://github.com/org/repo
repo_name	Name of the repository	buddy_Trading
helmchart_oci	Helm chart OCI registry path	oci://helm-charts/buddychart
helmchart_version	Version of the Helm chart	1.0.14
need_build_job	Flag to determine if the build stage is needed	true
2. Adding a New Pipeline
To add a new service pipeline:

Open the Constants class in the utils package.
Locate the NODEJS_BACKEND list.
Add a new service configuration following the existing format.
Example: Adding a New Service
If you want to add a service called awesome_service, update the NODEJS_BACKEND variable like this:

groovy
Copy code
static final NODEJS_BACKEND = [
    [
        name             : "buddy_Trading",
        image_name       : "buddy_Trading",
        repo_url         : "https://github.com/org/buddy_Trading",
        repo_name        : "buddy_Trading",
        helmchart_oci    : "oci://helm-charts/buddychart",
        helmchart_version: "1.0.14",
        need_build_job   : true
    ],
    [
        name             : "awesome_service",
        image_name       : "awesome_service",
        repo_url         : "https://github.com/org/awesome_service",
        repo_name        : "awesome_service",
        helmchart_oci    : "oci://helm-charts/awesomechart",
        helmchart_version: "1.2.0",
        need_build_job   : true
    ]
]
