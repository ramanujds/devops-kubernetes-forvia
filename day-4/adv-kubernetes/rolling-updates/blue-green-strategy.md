# Architecture

* `myapp-blue` → currently live
* `myapp-green` → new version
* `myapp-service` → routes traffic

Traffic switching happens by changing **Service selector**.

---

# Step 1 — Deploy BLUE Version (Live Version)

### blue-deployment.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp-blue
spec:
  replicas: 3
  selector:
    matchLabels:
      app: myapp
      version: blue
  template:
    metadata:
      labels:
        app: myapp
        version: blue
    spec:
      containers:
        - name: nginx
          image: nginx:1.25
          ports:
            - containerPort: 80
```

Apply:

```
kubectl apply -f blue-deployment.yaml
```

---

# Step 2 — Create Service (Pointing to BLUE)

### service.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: myapp-service
spec:
  type: ClusterIP
  selector:
    app: myapp
    version: blue
  ports:
    - port: 80
      targetPort: 80
```

Apply:

```
kubectl apply -f service.yaml
```

Check:

```
kubectl get pods -l version=blue
```

Traffic now goes to blue pods.

---

# Step 3 — Deploy GREEN Version (New Version)

Now deploy new version.

### green-deployment.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp-green
spec:
  replicas: 3
  selector:
    matchLabels:
      app: myapp
      version: green
  template:
    metadata:
      labels:
        app: myapp
        version: green
    spec:
      containers:
        - name: nginx
          image: nginx:1.26
          ports:
            - containerPort: 80
```

Apply:

```
kubectl apply -f green-deployment.yaml
```

Now both blue and green are running.

But traffic still goes to blue.

---

# Step 4 — Validate GREEN Before Switching

Check pods:

```
kubectl get pods -l version=green
```

Port forward to test manually:

```
kubectl port-forward deployment/myapp-green 8080:80
```

Open:

```
http://localhost:8080
```

If everything looks good — now switch.

---

# Step 5 — Switch Traffic to GREEN

Update service selector:

```
kubectl edit service myapp-service
```

Change:

```
version: blue
```

To:

```
version: green
```

OR apply updated YAML:

```yaml
selector:
  app: myapp
  version: green
```

Apply:

```
kubectl apply -f service.yaml
```

Done.

Traffic instantly switches.

No downtime.

---

# Step 6 — Rollback (If Something Breaks)

If green has issues:

```
kubectl edit service myapp-service
```

Switch back to:

```
version: blue
```

Traffic immediately returns to blue.

This is why blue/green is powerful.

---

# Step 7 — Cleanup Old Version

Once confident green is stable:

```
kubectl delete deployment myapp-blue
```

Now green becomes the new production baseline.

---

# How Service Makes This Possible

The magic is here:

```yaml
selector:
  app: myapp
  version: green
```

Service routes traffic based on labels.

It doesn’t care about Deployment names.
Only labels matter.

---

# Production Best Practice Improvements

In real production:

* Use readinessProbe
* Use external LoadBalancer or Ingress
* Use separate namespaces sometimes
* Monitor before switching
* Automate switching in CI/CD

If using:

NGINX Ingress Controller

You can switch traffic by updating Ingress backend instead of Service selector.

---

# Alternative Production Pattern (Safer)

Instead of editing Service:

Have:

* myapp-blue-service
* myapp-green-service

Then Ingress points to one.

Switch by editing Ingress.

Safer in large systems.

---

# Visual Flow

Initial:

Service → Blue Pods

After switch:

Service → Green Pods

Both versions run simultaneously.
Only traffic direction changes.

---

# Why This Is Better Than Rolling Update?

Rolling update:
Gradual replacement.

Blue/Green:
Instant switch.
Instant rollback.
Full environment validation before exposure.

But:
Uses double resources temporarily.

---


