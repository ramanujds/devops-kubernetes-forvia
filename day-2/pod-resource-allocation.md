# Why Resource Allocation Is Important

Kubernetes is a **multi-tenant scheduler**.

Multiple pods run on the same node:

```
Node
 ├── Pod A
 ├── Pod B
 ├── Pod C
```

If you don’t define limits:

* One pod can consume all CPU
* One memory spike can crash the node
* Other services get starved

So resource allocation provides:

✔ Fair scheduling
✔ Predictable performance
✔ Stability
✔ Better autoscaling
✔ OOM prevention

---

# Two Types of Resources

Kubernetes manages mainly:

| Resource | Meaning          |
|----------|------------------|
| CPU      | Processing power |
| Memory   | RAM              |

You define:

* **requests**
* **limits**

---

# Requests vs Limits

| Property | Purpose            |
|----------|--------------------|
| request  | Minimum guaranteed |
| limit    | Maximum allowed    |

## CPU Behavior

If:

```
request = 200m
limit = 500m
```

* Scheduler guarantees 200 millicores
* Pod can burst up to 500m
* If exceeds 500m → throttled (not killed)

---

## Memory Behavior

If:

```
request = 256Mi
limit = 512Mi
```

* Scheduler reserves 256Mi
* If pod exceeds 512Mi → **OOMKilled**

Memory limit violations kill pods.

---

# How to Allocate Resources

Inside a Deployment:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: asset-service
spec:
  replicas: 3
  template:
    spec:
      containers:
        - name: asset
          image: asset-service:1.0
          resources:
            requests:
              cpu: "200m"
              memory: "256Mi"
            limits:
              cpu: "500m"
              memory: "512Mi"
```

---

# What Happens Internally

When scheduling:

1. Kubernetes checks node capacity
2. Sums all pod **requests**
3. Places pod only if enough free resources exist

Limits are runtime enforcement.
Requests affect scheduling.

---

# Example

Assume Node has:

```
4 CPU
8Gi memory
```

Pods:

| Pod | CPU Request | Memory Request |
|-----|-------------|----------------|
| A   | 500m        | 1Gi            |
| B   | 500m        | 1Gi            |
| C   | 2 CPU       | 4Gi            |

Scheduler calculates totals before placing.

Without requests:

* Kubernetes may overcommit
* Performance collapses

---

# 7What Happens If You Don’t Define Resources?

Then:

* Pod runs in **BestEffort QoS**
* First candidate for eviction
* Unpredictable scheduling
* Very dangerous in production

---

# Resource Allocation & Autoscaling

Horizontal Pod Autoscaler (HPA) uses:

* CPU utilization based on **requests**

If request is too high:

* HPA under-scales

If request too low:

* HPA over-scales

Correct resource sizing is critical for autoscaling.

---

# How to Choose Proper Values

### Step 1

Deploy without limits in dev.

### Step 2

Monitor:

```
kubectl top pod
```

Or use Prometheus.

### Step 3

Set:

* Request = 70% of typical usage
* Limit = 120–150% of peak

---

# Special Case: Java Applications

For Spring Boot apps:

If memory limit is:

```
512Mi
```

You must tune JVM:

```
-XX:MaxRAMPercentage=75
```

Otherwise JVM may exceed limit → OOMKilled.

---

# Production Best Practices

✔ Always define requests
✔ Always define limits
✔ Avoid BestEffort
✔ Monitor usage before finalizing
✔ Align with HPA strategy
✔ Set JVM memory correctly

---
