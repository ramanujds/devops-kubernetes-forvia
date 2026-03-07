# 1. What is GitOps?

**GitOps** is an operational model where:

> **Git becomes the single source of truth for infrastructure and deployments.**

Instead of Jenkins applying changes directly to **Kubernetes**, the cluster automatically synchronizes with what is
defined in Git.

Example:

```text
Git Repository
   ↓
Kubernetes manifests / Helm charts
   ↓
ArgoCD watches the repo
   ↓
Cluster state is automatically synced
```

---

# 2. Traditional CI/CD deployments to Kubernetes often look like this:

Your current pipeline probably looks like this:

```text
GitHub
   ↓
Jenkins
   ↓
Build Docker Image
   ↓
Push to DockerHub
   ↓
kubectl apply
   ↓
AKS Deployment
```

Problems with this approach:

1. **Cluster state not visible in Git**
2. Hard to know **what version is deployed**
3. No automatic drift detection
4. Jenkins must have **cluster access**

---

# 3. GitOps Approach

With GitOps:

```text
GitHub
   ↓
Jenkins CI
   ↓
Build Docker Image
   ↓
Push Image
   ↓
Update Kubernetes manifest in Git
   ↓
ArgoCD detects change
   ↓
Cluster automatically updates
```

Jenkins **never talks to the cluster directly**.

ArgoCD does.

---

# 4. How ArgoCD Works

![Image](https://miro.medium.com/1%2AcTriV-5n67K_IVBvdn-dHg.png)

ArgoCD continuously monitors a Git repository.

It compares:

```text
Desired state (Git)
vs
Actual state (Kubernetes cluster)
```

If they differ, ArgoCD synchronizes the cluster.

Example:

```text
Git says replicas = 3
Cluster has replicas = 2
ArgoCD automatically fixes it
```

This is called **state reconciliation**.

---

# 5. Why GitOps is Powerful

### 1. Single Source of Truth

All deployments exist in Git.

Example repo:

```text
k8s-config
   ├── inventory-service
   │      deployment.yaml
   │      service.yaml
   ├── order-service
   └── gateway
```

You always know what is running in production.

---

### 2. Easy Rollbacks

Rollback becomes a Git revert.

Example:

```bash
git revert commit
```

ArgoCD will automatically redeploy the previous version.

No manual cluster operations needed.

---

### 3. Audit Trail

Every deployment change is visible:

```text
Git commit history
```

Example:

```text
commit 1
image: inventory:v1

commit 2
image: inventory:v2
```

This is extremely important for **enterprise compliance**.

---

### 4. Drift Detection

If someone changes something manually:

```bash
kubectl scale deployment inventory --replicas=10
```

ArgoCD will detect drift and revert it back to what Git says.

This prevents configuration chaos.

---

### 5. Secure Cluster Access

Without GitOps:

```text
Jenkins → Kubernetes cluster
```

This means Jenkins needs cluster admin access.

With GitOps:

```text
Jenkins → Git
ArgoCD → Kubernetes
```

Cluster access stays inside the cluster.

Much safer.

---

# 6. Use Cases for GitOps

### Use Case 1 — Microservices Platform

Company with 50 microservices.

Without GitOps:

```text
50 Jenkins pipelines modifying cluster
```

Very difficult to track.

With GitOps:

```text
Git repo stores deployment manifests
ArgoCD manages all services
```

Centralized control.

---

### Use Case 2 — Multi-Environment Deployment

Example environments:

```text
dev
qa
staging
prod
```

Repo structure:

```text
env
 ├── dev
 ├── qa
 ├── staging
 └── prod
```

Promoting code becomes:

```bash
git merge dev → qa
git merge qa → prod
```

ArgoCD deploys automatically.

---

### Use Case 3 — Disaster Recovery

If the cluster crashes:

```text
New cluster created
Install ArgoCD
Point to Git repo
```

Entire infrastructure redeploys automatically.

This is huge for reliability.

---

# 7. GitOps Pipeline Architecture

Modern cloud-native pipelines look like this:

```text
Developer
   ↓
GitHub
   ↓
Jenkins CI
   ↓
Docker Build
   ↓
Push Image
   ↓
Update Helm values in Git
   ↓
ArgoCD
   ↓
Kubernetes Deployment
```

CI builds images.

CD is handled by ArgoCD.

---

# 8. Example GitOps Deployment Change

Current manifest:

```yaml
image: ramanuj/inventory-service:v1
```

Pipeline updates:

```yaml
image: ramanuj/inventory-service:v2
```

Commit:

```bash
git commit -m "deploy inventory v2"
```

ArgoCD detects change and deploys automatically.

---

