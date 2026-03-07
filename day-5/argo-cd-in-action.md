## Using Argo CD Step-by-Step

Below is a **practical step-by-step flow** that fits perfectly with the pipeline you already built:

* Jenkins builds image
* pushes to DockerHub
* ArgoCD deploys to **Kubernetes**

---

# 1. Install ArgoCD in AKS

First create a namespace.

```bash
kubectl create namespace argocd
```

Install ArgoCD using the official manifest.

```bash
kubectl apply -n argocd \
-f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

Check pods:

```bash
kubectl get pods -n argocd
```

You should see components like:

```
argocd-server
argocd-repo-server
argocd-application-controller
```

---

# 2. Access ArgoCD UI

Expose the service.

```bash
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

Open browser:

```
http://localhost:8080
```

---

# 3. Get ArgoCD Admin Password

Retrieve initial password.

```bash
kubectl get secret argocd-initial-admin-secret \
-n argocd \
-o jsonpath="{.data.password}" | base64 --decode
```

Login:

```
username: admin
password: <decoded password>
```

You now see the **ArgoCD dashboard**.

---

# 4. Create a Git Repository for Kubernetes Manifests

Example repo structure:

```
k8s-manifests
   ├── inventory
   │     deployment.yaml
   │     service.yaml
```

Example `deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: part-inventory-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: inventory
  template:
    metadata:
      labels:
        app: inventory
    spec:
      containers:
        - name: inventory
          image: ramanuj/part-inventory-service:1
          ports:
            - containerPort: 8080
```

Example `service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: inventory-service
spec:
  type: LoadBalancer
  selector:
    app: inventory
  ports:
    - port: 80
      targetPort: 8080
```

Push this repo to GitHub.

---

# 5. Create an ArgoCD Application

You can create the application from CLI or UI.

Example YAML:

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: inventory-service
  namespace: argocd
spec:
  project: default

  source:
    repoURL: https://github.com/yourrepo/k8s-manifests
    targetRevision: HEAD
    path: inventory

  destination:
    server: https://kubernetes.default.svc
    namespace: default

  syncPolicy:
    automated:
      prune: true
      selfHeal: true
```

Apply:

```bash
kubectl apply -f argocd-app.yaml
```

ArgoCD now tracks the repo.

---

# 6. Deploy Application

Once the application is created:

```
ArgoCD UI
   ↓
Application appears
   ↓
Click Sync
```

ArgoCD applies:

```
deployment.yaml
service.yaml
```

Verify:

```bash
kubectl get pods
kubectl get svc
```

Your service should be running.

---

# 7. Integrate with Jenkins Pipeline

Your Jenkins pipeline already pushes images.

Now modify pipeline to **update the image tag in Git** instead of running `kubectl`.

Example stage:

```groovy
stage('Update GitOps Repo') {
    steps {
        sh '''
        git clone https://github.com/yourrepo/k8s-manifests
        cd k8s-manifests/inventory

        sed -i '' "s|image: ramanuj/part-inventory-service:.*|image: ramanuj/part-inventory-service:${BUILD_NUMBER}|" deployment.yaml

        git commit -am "Deploy version ${BUILD_NUMBER}"
        git push
        '''
    }
}
```

Pipeline becomes:

```
Jenkins
   ↓
Build Docker Image
   ↓
Push Image
   ↓
Update manifest in Git
   ↓
ArgoCD detects change
   ↓
Deployment updated
```

---

# 8. Enable Auto Sync

In the Application YAML we used:

```yaml
syncPolicy:
  automated:
    prune: true
    selfHeal: true
```

This enables:

| Feature     | What it does                   |
|-------------|--------------------------------|
| Auto deploy | Git change triggers deployment |
| Self heal   | fixes manual cluster changes   |
| Prune       | deletes removed resources      |

---

# 9. Verify Deployment

Check pods:

```bash
kubectl get pods
```

Check service:

```bash
kubectl get svc
```

Access via external IP.

---

# 10. Final Architecture

Your system now becomes:

```
Terraform
   ↓
Creates AKS

Jenkins
   ↓
Build Docker Image
   ↓
Push Image

GitOps Repo
   ↓
Kubernetes manifests

ArgoCD
   ↓
Deploys to AKS
```

This is the **modern Kubernetes CI/CD architecture** used in most companies.

---
