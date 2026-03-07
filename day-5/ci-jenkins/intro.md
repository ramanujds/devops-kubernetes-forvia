## Jenkins CI (Continuous Integration)

**Jenkins** is one of the most widely used tools for **Continuous Integration (CI)** and **Continuous Delivery (CD)**.
It automates the process of building, testing, and validating code whenever developers push changes to a repository.

Since you work with **microservices, DevOps, and Kubernetes**, Jenkins is often used in real-world pipelines for
automating build and deployment workflows.

---

# 1. What is Continuous Integration (CI)?

**Continuous Integration** means:

> Developers frequently merge code into a shared repository, and every change automatically triggers a build and tests.

Instead of integrating code at the end of development, it happens **multiple times a day**.

### Example Workflow

1. Developer pushes code to GitHub/GitLab
2. Jenkins detects the change
3. Jenkins triggers a pipeline
4. Code is compiled
5. Unit tests run
6. Artifacts are generated (JAR, Docker image)

If something fails → developers get notified immediately.

---

# 2. Why Jenkins for CI?

Main problems Jenkins solves:

| Problem            | Solution by Jenkins    |
|--------------------|------------------------|
| Manual builds      | Automated builds       |
| Late bug detection | Immediate feedback     |
| Integration issues | Continuous integration |
| Deployment delays  | Automated pipeline     |

---

# 3. Jenkins CI Architecture

Typical Jenkins architecture contains:

### Jenkins Components

**1. Jenkins Server (Controller)**
Central server that manages pipelines.

**2. Agents (Workers)**
Machines that execute jobs.

Example:

```
Developer → GitHub
              ↓
           Jenkins
              ↓
      Build + Test
              ↓
        Docker Image
              ↓
        Artifact Repo
```

---

# 4. Jenkins CI Pipeline Flow

A typical **CI pipeline** looks like this:

```
Git Push
   ↓
Checkout Code
   ↓
Compile
   ↓
Run Unit Tests
   ↓
Code Quality Scan
   ↓
Build Artifact
   ↓
Push Artifact
```

Example:

```
GitHub → Jenkins → Maven Build → Unit Tests → Docker Build → Push to Registry
```

---

# 5. Jenkins Pipeline Types

### 1. Freestyle Job

Old style Jenkins jobs.

Configured via UI.

Example steps:

```
1. Git checkout
2. Maven build
3. Run tests
```

Not recommended for modern DevOps.

---

### 2. Pipeline as Code

Pipeline is written in a **Jenkinsfile**.

Stored in the repository.

Example:

```groovy
pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                git 'https://github.com/example/project.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
    }
}
```

Advantages:

* Version controlled
* Reproducible
* Easier collaboration

---

# 6. CI Pipeline for a Microservice (Production Architecture)

### Pipeline Steps

1. Code push
2. Jenkins build
3. Unit tests
4. Build Docker image
5. Push to container registry

Example pipeline:

```groovy
pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                git 'https://github.com/company/order-service.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t order-service:1.0 .'
            }
        }

        stage('Push Image') {
            steps {
                sh 'docker push myrepo/order-service:1.0'
            }
        }

    }
}
```

---

# 7. Jenkins CI + Kubernetes

In cloud-native environments:

```
GitHub
   ↓
Jenkins Pipeline
   ↓
Build Docker Image
   ↓
Push to Registry
   ↓
Deploy to Kubernetes
```

Example tools involved:

| Step             | Tool            |
|------------------|-----------------|
| Code repository  | GitHub          |
| CI               | Jenkins         |
| Build            | Maven           |
| Containerization | Docker          |
| Registry         | ECR / DockerHub |
| Deployment       | Kubernetes      |
| CD               | ArgoCD          |

This is the typical **GitOps pipeline** used in production.

---

# 8. Jenkins Plugins

Jenkins has **2000+ plugins**.

Common CI plugins:

| Plugin            | Use             |
|-------------------|-----------------|
| Git Plugin        | Git integration |
| Docker Plugin     | Docker builds   |
| Kubernetes Plugin | Dynamic agents  |
| SonarQube Plugin  | Code quality    |
| Slack Plugin      | Notifications   |

---

# 9. Jenkins CI Example (Enterprise Setup)

Example architecture for a company:

```
Developer → GitHub
              ↓
        Jenkins Webhook
              ↓
        Jenkins Pipeline
              ↓
     Build + Unit Tests
              ↓
       SonarQube Scan
              ↓
        Docker Build
              ↓
     Push to DockerHub/ECR
```

Then **CD tool like ArgoCD deploys it**.

---

# 10. Best Practices for Jenkins CI

### 1. Use Pipeline as Code

Avoid freestyle jobs.

### 2. Use Docker agents

Example:

```groovy
agent {
    docker {
        image 'maven:3.9'
    }
}
```

Ensures consistent builds.

---

### 3. Use Webhooks

Instead of polling:

```
GitHub → Webhook → Jenkins
```

Build triggers instantly.

---

### 4. Separate CI and CD

Example:

```
Jenkins → Build
ArgoCD → Deployment
```

Modern DevOps architecture.

---

# 11. Jenkins vs GitHub Actions vs GitLab CI

| Tool           | Type        |
|----------------|-------------|
| Jenkins        | Self-hosted |
| GitHub Actions | Cloud CI    |
| GitLab CI      | Built-in CI |
| CircleCI       | Cloud CI    |

Jenkins still dominates **enterprise environments** because of flexibility.

---

# 12. Simple CI Example

For teaching DevOps:

```
Spring Boot Microservice
        ↓
GitHub Repo
        ↓
Jenkins CI
        ↓
Maven Build
        ↓
Docker Image
        ↓
DockerHub
```



