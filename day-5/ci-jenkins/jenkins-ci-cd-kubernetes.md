# Overall Flow

Your CI/CD pipeline will look like this:

```
GitHub
   ↓
Jenkins CI
   ↓
Docker Build
   ↓
Docker Push (DockerHub)
   ↓
kubectl apply
   ↓
AKS Deployment Updated
```

---

# Step 1 — Create AKS Cluster (If not already created)

# Step 2 — Create Kubernetes Deployment YAML

Example `deployment.yaml`.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: part-inventory-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: part-inventory-service
  template:
    metadata:
      labels:
        app: part-inventory-service
    spec:
      containers:
        - name: part-inventory-service
          image: ramanuj/part-inventory-service:latest
          ports:
            - containerPort: 8080
```

---

# Step 3 — Create Service

`service.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: part-inventory-service
spec:
  type: LoadBalancer
  selector:
    app: part-inventory-service
  ports:
    - port: 80
      targetPort: 8080
```

Apply them:

```bash
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
```

Check service:

```bash
kubectl get svc
```

AKS will create a **public LoadBalancer IP**.

---

# Step 4 — Allow AKS to Pull from DockerHub

If your Docker image is **public**, no action needed.

If private:

```bash
kubectl create secret docker-registry dockerhub-secret \
--docker-username=yourusername \
--docker-password=yourtoken \
--docker-email=your@email.com
```

Then update deployment:

```yaml
spec:
  imagePullSecrets:
    - name: dockerhub-secret
```

---

# Step 5 — Store AKS Credentials in Jenkins

Jenkins must access the cluster.

Add credentials:

```
Manage Jenkins
 → Credentials
 → Add Credentials
```

Store:

```
kubeconfig file
```

Credential ID example:

```
aks-kubeconfig
```

---

# Step 8 — Add Deployment Stage in Jenkins

After pushing Docker image, add this stage.

```groovy
stage('Deploy to AKS') {
    steps {
        withCredentials([file(credentialsId: 'aks-kubeconfig', variable: 'KUBECONFIG')]) {
            sh '''
            export PATH=$PATH:/usr/local/bin:/opt/homebrew/bin

            kubectl set image deployment/part-inventory-service \
            part-inventory-service=${IMAGE_NAME}:${IMAGE_TAG}

            kubectl rollout status deployment/part-inventory-service
            '''
        }
    }
}
```

This updates the deployment with the **new image tag**.

---

# Step 9 — Verify Deployment

Check pods:

```bash
kubectl get pods
```

Check service:

```bash
kubectl get svc
```

Open browser:

```
http://EXTERNAL-IP
```

Your microservice should be running.

---

# Final Jenkins Pipeline Flow

```
Stage 1 → Checkout Code
Stage 2 → Build Docker Image
Stage 3 → Push Image to DockerHub
Stage 4 → Deploy to AKS
```

Result:

```
New Docker image
      ↓
Deployment updated
      ↓
Rolling update on AKS
```

Kubernetes will perform a **rolling update with zero downtime**.

---

