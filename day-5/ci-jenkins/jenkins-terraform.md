# 1. Recommended Architecture

Separate **Infrastructure CI/CD** from **Application CI/CD**.

```
Repo 1: Infrastructure
(terraform)
        ↓
Terraform Pipeline
        ↓
Creates AKS

Repo 2: Application
(Spring Boot / Docker)
        ↓
Jenkins CI Pipeline
        ↓
Build Image
        ↓
Push Image
        ↓
Deploy to AKS
```

This prevents accidental cluster recreation during app deployments.

---

# 2. Infrastructure Pipeline (Terraform)

Your Terraform pipeline should do:

```
Terraform Init
Terraform Validate
Terraform Plan
Terraform Apply
```

Example Jenkins stage:

```groovy
stage('Terraform Init') {
    steps {
        sh 'terraform init'
    }
}

stage('Terraform Plan') {
    steps {
        sh 'terraform plan'
    }
}

stage('Terraform Apply') {
    steps {
        sh 'terraform apply -auto-approve'
    }
}
```

Terraform provisions:

```
Resource Group
AKS Cluster
Node Pool
Networking
```

---

# 3. Store Terraform State Properly

Never store state locally.

Use **remote backend** like Azure Storage.

Example:

```hcl
terraform {
  backend "azurerm" {
    resource_group_name  = "tf-state-rg"
    storage_account_name = "tfstate123"
    container_name       = "tfstate"
    key                  = "aks-cluster.tfstate"
  }
}
```

This ensures safe collaboration.

---

# 4. Pass AKS Credentials to Jenkins

After Terraform creates AKS, Jenkins must access the cluster.

Run:

```
az aks get-credentials --resource-group rg --name aks
```

Store the resulting **kubeconfig** in Jenkins credentials.

Then your deployment stage works exactly like before.

---

# 5. Integrate Terraform and App Pipelines

There are three common approaches.

---

## Approach 1

Infrastructure pipeline runs **once or rarely**.

```
Terraform Pipeline
       ↓
Creates AKS

Application Pipeline
       ↓
Build Image
       ↓
Deploy to AKS
```

Simple and stable.

---

## Approach 2 (Trigger Terraform Before Deploy)

Application pipeline checks infrastructure.

```
Stage 1 → Terraform Apply
Stage 2 → Build Image
Stage 3 → Deploy
```

Useful when infrastructure changes frequently.

But slower.

---

## Approach 3

Use separate repos and trigger pipelines.

```
Terraform Repo
      ↓
Infra Pipeline

Application Repo
      ↓
App Pipeline
```

Infra pipeline runs only when Terraform changes.

---

# Ideal CI/CD Architecture

```
                GitHub
                   │
        ┌──────────┴──────────┐
        │                     │
Terraform Repo           Application Repo
        │                     │
Terraform Pipeline       Jenkins CI
        │                     │
Creates AKS             Docker Build
        │                     │
Azure Infrastructure     Docker Push
                              │
                         Deploy to AKS
```

---

# 8. Use GitOps with Argo CD for Kubernetes Deployments

Instead of using `kubectl apply` directly from Jenkins, most teams use **GitOps** with **Argo CD**.

Flow becomes:

```
Jenkins
   ↓
Build Docker Image
   ↓
Update Helm chart
   ↓
Commit to Git
   ↓
ArgoCD deploys automatically
```

Advantages:

* full audit trail
* safer deployments
* rollback support
* production-grade Kubernetes CD

