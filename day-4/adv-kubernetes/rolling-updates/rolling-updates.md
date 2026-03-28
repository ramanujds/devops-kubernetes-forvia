# Who Handles Rolling Updates?

Rolling updates are managed by:

Kubernetes Deployment Controller

When you change:

* image tag
* env variable
* resource limits
* replicas
* anything inside `.spec.template`

Kubernetes detects a change in Pod template and triggers a rolling update.

---

# What Actually Happens During a Rolling Update?

Let’s say:

You have:

```
replicas: 4
image: myapp:v1
```

You update to:

```
image: myapp:v2
```

Here’s what Kubernetes does internally:

1. Creates a new ReplicaSet (for v2)
2. Gradually scales up new ReplicaSet
3. Gradually scales down old ReplicaSet
4. Maintains availability constraints

This continues until all 4 pods run v2.

Zero downtime — if configured correctly.

---

# Under the Hood

Deployment manages:

Old ReplicaSet (v1)
New ReplicaSet (v2)

Traffic is routed through Service.

Since Service selects by label (like `app: myapp`), both old and new pods match during transition.

That’s how traffic keeps flowing.

---

# Default Rolling Update Behavior

By default:

```
strategy:
  type: RollingUpdate
```

And defaults are:

```
maxUnavailable: 25%
maxSurge: 25%
```

Let’s understand these properly.

---

# 1. maxUnavailable

Maximum number of pods that can be unavailable during update.

If replicas = 4

25% of 4 = 1 pod

So at most 1 pod can be down.

---

# 2. maxSurge

Extra pods allowed above desired replicas.

If replicas = 4

25% = 1

So Kubernetes can temporarily create 5 pods.

This helps ensure availability.

---

# Example Rolling Update Flow (replicas = 4)

Initial state:
4 pods running v1

Step 1:
Create 1 new pod (v2) → total 5 pods

Step 2:
Delete 1 old pod → total 4 pods

Repeat until complete.

Smooth transition.

---

# How To Configure Rolling Updates

Here’s the YAML:

```yaml
apiVersion: apps/v1
kind: Deployment
spec:
  replicas: 4
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 2
```

You can also use percentage:

```
maxUnavailable: 25%
maxSurge: 50%
```

---

# Extreme Configurations (For Understanding)

## Zero Downtime Strict Mode

```yaml
maxUnavailable: 0
maxSurge: 1
```

Meaning:

* Never reduce running pods
* Always create new before deleting old

This is safest for production.

---

## Fast But Risky Mode

```yaml
maxUnavailable: 4
maxSurge: 0
```

This will kill all old pods first.

Downtime likely.

---

# What Triggers a Rolling Update?

Only changes in:

```
.spec.template
```

Examples:

* image change
* env change
* resource limits
* label inside pod template

If you change only replicas count:

That does NOT create new ReplicaSet.
It just scales.

---

# How Kubernetes Knows Pods Are Ready

Rolling update depends heavily on:

* readinessProbe
* livenessProbe
* startupProbe

If readinessProbe is missing:

Kubernetes may mark pod ready too early.

Then traffic goes to unhealthy pod.

Boom — downtime.

So readinessProbe is critical.

---

# Controlling Update Speed

You can also configure:

```
minReadySeconds: 30
```

This ensures pod must stay Ready for 30 seconds before considered stable.

Prevents instant delete of old pods.

---

# What If Update Fails?

If new pods:

* Crash
* Fail readiness
* Image pull error

Deployment status becomes:

```
ProgressDeadlineExceeded
```

You can configure:

```
progressDeadlineSeconds: 600
```

After that, update marked failed.

You manually rollback:

```
kubectl rollout undo deployment myapp
```

---

# Rollout Commands (Very Important)

Check status:

```
kubectl rollout status deployment myapp
```

See history:

```
kubectl rollout history deployment myapp
```

Rollback:

```
kubectl rollout undo deployment myapp
```

Rollback to specific revision:

```
kubectl rollout undo deployment myapp --to-revision=2
```

---

# How This Differs From Blue/Green

Rolling Update:
Gradual replacement.

Blue/Green:
Two separate environments.
Traffic switch.

Rolling update uses same Service.
Blue/Green switches Service selector.

---

# Real Production Best Practice

For stateless apps:

```
maxUnavailable: 0
maxSurge: 1
readinessProbe mandatory
minReadySeconds set
```

For stateful apps:
Be careful.
Rolling updates can break order unless using StatefulSet.

---
