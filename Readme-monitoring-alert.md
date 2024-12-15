Grafana: monitor connection websocket ram cpu high ....
Robusta: alert pods restart

# Monitoring Configuration for Kubernetes (values.yaml)

This file defines the configuration for monitoring tools deployed on a Kubernetes cluster. It includes configurations for Grafana, Alertmanager, and Prometheus, along with data persistence, ingress, and alerting rules.

---

## Components Overview

### 1. **Grafana**
Grafana is enabled and configured for visualization and monitoring.

- **Plugins**:
  - Includes the `golioth-websocket-datasource` plugin.

- **Persistence**:
  - Persistent storage is enabled using a PersistentVolumeClaim with `1Gi` storage.
  - `storageClassName`: `do-block-storage`.

- **Ingress**:
  - Ingress is enabled to expose Grafana externally with TLS security.
  - **Host**: `monitor.staging.tradingos.com`.
  - TLS is configured with a secret named `grafana.staging.tradingos.com.grafana-general-tls-gvpxt`.
  - Configured annotations for SSL redirection and certificate management using `cert-manager`.

- **Additional DataSources**:
  - Configured a Loki datasource:
    - **Name**: `loki`
    - **URL**: `http://loki:3100`
    - **Access**: `proxy`
    - **Default**: `false`

### 2. **Alertmanager**
Alertmanager is enabled for managing alerts and notifications.

- **Persistence**:
  - Persistent storage is configured with `1Gi` storage using `do-block-storage`.

- **SMTP Configuration**:
  - Alerts are sent via email using an SMTP server.
  - **Sender**: `care@tradingstudio.com`.
  - **SMTP Server**: `****************.amazonaws.com:465`.

- **Inhibit Rules**:
  - Suppresses low-priority alerts (e.g., `info` and `warning`) if higher-priority alerts (`critical`) exist for the same namespace or alert.

- **Receivers**:
  - Configured `gmail-notifications` to send alert emails to `tradingos-alert@tradingstudio.com`.

- **Route Configuration**:
  - Alerts are grouped by `alertname` and sent to the `gmail-notifications` receiver.
  - Watchdog alerts are routed to an empty receiver.

- **Templates**:
  - Custom templates for alerts are defined in `/etc/alertmanager/config/*.tmpl`.

- **Retention**:
  - Alert history is retained for `300h`.

- **Affinity**:
  - Alertmanager pods are scheduled on nodes labeled with `trading-monitor`.

### 3. **Prometheus**
Prometheus is enabled for metrics collection and monitoring.

- **External Labels**:
  - Adds the external label `cluster: tradingos-staging` to all metrics.

- **Retention**:
  - Metrics data is retained for `90 days`.

- **Storage**:
  - Persistent storage of `20Gi` is configured using `do-block-storage`.

- **Scrape metrics**:
prometheus:
  enabled: true
  prometheusSpec:
    additionalScrapeConfigs:
      - job_name: web-socket
        scrape_interval: 30s
        static_configs:
          - targets: [ 'websocket-service-tech-backend.websocket.svc.cluster.local:8080' ] ## endpoint metrics dns k8s
        metrics_path: '/metrics'
---

## Deployment Requirements

### Persistent Volumes
Ensure your Kubernetes cluster supports the storage class `do-block-storage` for Grafana, Alertmanager, and Prometheus to persist data.

### Ingress Configuration
- **TLS Certificates**:
  - TLS secrets (e.g., `grafana.staging.tradingos.com.grafana-general-tls-gvpxt`) should be pre-created or managed using `cert-manager`.
- **Ingress Controller**:
  - Ensure an ingress controller (e.g., NGINX) is installed and configured.

### SMTP Configuration
Replace placeholders (`****************`) with actual credentials for the SMTP server used by Alertmanager.

---

## Customization

1. **Grafana**:
   - Add additional data sources under `grafana.additionalDataSources` as required.
   - Modify ingress annotations to match your ingress controller's requirements.

2. **Alertmanager**:
   - Update `smtp_from`, `smtp_smarthost`, and `smtp_auth_*` fields with your SMTP details.
   - Add more receivers or routes in the `alertmanager.config` section.

3. **Prometheus**:
   - Adjust `retention` and `storageSpec` based on your cluster's storage capacity and monitoring needs.

---

## Deployment Instructions
# Monitoring Stack Deployment Using Helmfile

This guide explains how to deploy a Prometheus-based monitoring stack, including Grafana and Alertmanager, using Helmfile. The configuration leverages a `values.yaml` file for custom settings.

---

## Prerequisites

Before you begin, ensure you have:

1. **Helm**: 
   - Helm v3 or later installed. [Helm Installation Guide](https://helm.sh/docs/intro/install/)

2. **Helmfile**:
   - Installed Helmfile on your system. [Helmfile Installation Guide](https://github.com/helmfile/helmfile#installation)

3. **Kubernetes Cluster**:
   - A running Kubernetes cluster.

4. **Namespace**:
   - The `monitoring` namespace will be created automatically if it does not already exist.

Run command: 
cd /devops-minitest/monitoring-alert/prometheus-stack
Helm apply
--------------
Robusta
# Robusta Monitoring Configuration
This file contains the configuration for the Robusta monitoring framework, designed to enhance Kubernetes monitoring and alerting. The configuration integrates with Discord for notifications, defines custom playbooks for handling events, and includes platform playbooks for advanced use cases.
Robusta - Kubernetes Monitoring and Alerts
Robusta is a powerful Kubernetes monitoring framework designed for real-time event alerts and automated issue resolution. It enhances cluster monitoring with the following key features:

Pod Restart Alerts: Automatically detects and alerts on Pod restarts, providing detailed context (e.g., reasons like CrashLoopBackOff or OOMKilled).
Discord Integration: Sends alert notifications directly to Discord channels via webhooks for immediate team awareness.
Custom Playbooks: Automates responses to critical events, such as analyzing Pod logs or identifying resource bottlenecks.
Prometheus and Grafana Integration: Works seamlessly with existing monitoring stacks for metrics visualization and additional alerting.
Advanced Filtering: Allows namespace-specific alerting and fine-tuned scope definitions.

---
## Key Features

1. **Discord Integration**:
   - Alerts and notifications are sent to a Discord webhook.
   - Configure the `discord_sink` with the desired `name` and `webhook URL`.

2. **Global Parameters**:
   - `clusterName`: Defines the name of the monitored cluster.
   - `clusterZone`: Specifies the cluster's zone.
   - `global.clusterDomain`: Sets the cluster domain (default is `cluster.local`).

3. **Custom Playbooks**:
   - Predefined playbooks for specific Kubernetes issues like `CrashLoopBackOff` and `PodOOMKill`.
   - Automated responses for critical Kubernetes events.

4. **Platform Playbooks**:
   - Advanced playbooks for Kubernetes warning events, resource change tracking, job failures, and scheduled tasks.

5. **Scheduler and Maintenance**:
   - Weekly resource scans (KRR).
   - Regular cleanup of old Robusta pods.

6. **Runner Image**:
   - Uses the `robusta-runner:0.19.0` image for executing actions.

---
## Robusta
## Configuration Details

### 1. **Discord Sink**

The `discord_sink` defines the Discord webhook for alert notifications.

```yaml
sinksConfig:
- discord_sink:
    name: <discord_name>
    url: <webhook>
    scope:
      include:
        - namespace: "namespace need alert"

Run command: 
cd /devops-minitest/monitoring-alert/robusta
Helm apply