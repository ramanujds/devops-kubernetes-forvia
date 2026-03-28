# Gateway API – Step-by-Step Guide

# Step 1 – Enable Kubernetes in Docker Desktop

Make sure Kubernetes is enabled.

Check:

```bash
kubectl get nodes
```

You should see:

```
docker-desktop
```

---

# Step 2 – Install Gateway API CRDs

Gateway API is not built-in. Install official CRDs:

```bash
kubectl apply -f https://github.com/kubernetes-sigs/gateway-api/releases/latest/download/standard-install.yaml
```

Verify:

```bash
kubectl get crds | grep gateway
```

You should see:

* gateways.gateway.networking.k8s.io
* httproutes.gateway.networking.k8s.io
* gatewayclasses.gateway.networking.k8s.io

Good. API is ready.

---

# Step 3 – Install Gateway Controller

Gateway API needs a controller (like Ingress needs one).

We’ll use:

NGINX Gateway Fabric

Install it:

```bash
kubectl apply -f https://raw.githubusercontent.com/nginxinc/nginx-gateway-fabric/main/deploy/crds.yaml
kubectl apply -f https://raw.githubusercontent.com/nginxinc/nginx-gateway-fabric/main/deploy/default/deploy.yaml
```

Check pods:

```bash
kubectl get pods -n nginx-gateway
```

Wait until running.

---

# Step 4 – Create Demo Applications

We’ll use simple nginx containers.

## app1.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: app1
  template:
    metadata:
      labels:
        app: app1
    spec:
      containers:
        - name: app1
          image: nginx
          ports:
            - containerPort: 80
---
apiVersion: v1
kind: Service
metadata:
  name: app1-service
spec:
  selector:
    app: app1
  ports:
    - port: 80
      targetPort: 80
```

Duplicate for app2 (rename app1 → app2).

Apply:

```bash
kubectl apply -f app1.yaml
kubectl apply -f app2.yaml
```

---

# Step 5 – Create GatewayClass

This tells Kubernetes which controller manages Gateway.

## gatewayclass.yaml

```yaml
apiVersion: gateway.networking.k8s.io/v1
kind: GatewayClass
metadata:
  name: nginx
spec:
  controllerName: gateway.nginx.org/nginx-gateway-controller
```

Apply:

```bash
kubectl apply -f gatewayclass.yaml
```

---

# Step 6 – Create Gateway

This defines listener (like load balancer).

## gateway.yaml

```yaml
apiVersion: gateway.networking.k8s.io/v1
kind: Gateway
metadata:
  name: my-gateway
spec:
  gatewayClassName: nginx
  listeners:
    - name: http
      protocol: HTTP
      port: 80
```

Apply:

```bash
kubectl apply -f gateway.yaml
```

Check:

```bash
kubectl get gateway
```

Status should show programmed = true.

---

# Step 7 – Create HTTPRoute

Now attach routes to Gateway.

## httproute.yaml

```yaml
apiVersion: gateway.networking.k8s.io/v1
kind: HTTPRoute
metadata:
  name: app-route
spec:
  parentRefs:
    - name: my-gateway
  rules:
    - matches:
        - path:
            type: PathPrefix
            value: /app1
      backendRefs:
        - name: app1-service
          port: 80
    - matches:
        - path:
            type: PathPrefix
            value: /app2
      backendRefs:
        - name: app2-service
          port: 80
```

Apply:

```bash
kubectl apply -f httproute.yaml
```

---

# 8️⃣ Step 8 – Configure Local Host Mapping

Edit:

```
/etc/hosts
```

Add:

```
127.0.0.1 mygw.local
```

---

# Step 9 – Test

Open browser:

```
http://mygw.local/app1
http://mygw.local/app2
```

If it doesn't resolve automatically, check service created by gateway controller:

```bash
kubectl get svc -A
```

You’ll see something like:

```
nginx-gateway   nginx-gateway   NodePort
```

If needed, access via:

```
http://localhost:<nodeport>/app1
```

---

# What Just Happened (Architect View)

Here’s the layered separation:

```
GatewayClass → tells which controller
Gateway → defines listener (infra)
HTTPRoute → defines routing logic
Service → forwards to pods
```

Now compare with Ingress:

Ingress bundles everything together.

Gateway API splits responsibilities.

This is huge in enterprise multi-team setups.

---

# How This Differs from Ingress

With Ingress:

```
Ingress object = Listener + Routing + TLS
```

With Gateway:

```
Gateway = Listener + TLS
HTTPRoute = Routing
```

Cleaner. Safer. More modular.

---

# Real Enterprise Pattern

Infra team:

* Creates Gateway
* Controls TLS
* Controls ports

App team:

* Creates HTTPRoute
* Only controls path routing

No messing with certificates.

This is why Gateway API is future direction.

