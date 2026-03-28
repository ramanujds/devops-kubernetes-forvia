# Helm Architecture (Helm v3)

First important thing:

There are two major versions:

* Helm v2 (Old architecture – had Tiller)
* Helm v3 (Current – No Tiller, simplified, more secure)

We’ll focus on **Helm v3**, because that’s what’s used today.

---

## High-Level Architecture

Helm architecture is actually simple.

It has **3 main components**:

1. Helm CLI
2. Kubernetes API Server
3. Release storage (inside cluster)

Let’s explain each clearly.

---

## 1. Helm CLI (Client)

This is what you install locally.

When you run:

```
helm install myapp ./chart
```

The Helm CLI does:

* Reads the chart
* Renders templates using values.yaml
* Generates final Kubernetes YAML
* Sends it to Kubernetes API server

Important:
All templating happens on the client side.

No server component.

---

## 2. Kubernetes API Server

Helm does NOT deploy things directly.

It sends rendered YAML to:

Kubernetes API Server

From there:

* Scheduler schedules pods
* Controller Manager manages replicas
* etcd stores state

Helm just acts like a smart YAML generator + release manager.

---

## 3. Release Metadata Storage

Here’s something interesting.

When you install a chart:

```
helm install myapp ./chart
```

Helm stores release information inside the cluster as:

* Kubernetes Secrets (default)
* Or ConfigMaps (optional)

This includes:

* Chart version
* Values used
* Manifest rendered
* Revision history

That’s how rollback works.

---

# Helm v3 Architecture Diagram (Conceptual)

User
↓
Helm CLI
↓
Template Rendering
↓
Kubernetes API Server
↓
Cluster Resources (Pods, Services, etc.)
↓
Release stored as Secret

Simple. Clean. Secure.

---

# What About Helm v2?

This is good interview knowledge.

Helm v2 had:

* Helm CLI
* Tiller (server-side component running inside cluster)

Tiller:

* Had cluster-wide access
* Rendered templates
* Managed releases

Problem:

* Security risk
* Required RBAC complexity
* Not multi-tenant friendly

Helm v3 removed Tiller completely.

Now:

* Rendering happens client-side
* Uses your kubeconfig credentials
* Respects Kubernetes RBAC

Much safer.

---

# How Helm Internally Works (Step-by-Step Flow)

Let’s walk through an install.

### Step 1 – User Runs Command

```
helm install myapp nginx-chart
```

### Step 2 – Chart Is Loaded

Helm reads:

* Chart.yaml
* values.yaml
* templates/

### Step 3 – Template Engine Runs

Helm uses Go template engine.

Example:

```
replicas: {{ .Values.replicaCount }}
```

Values get substituted.

Final YAML is generated.

You can test this without deploying:

```
helm template ./chart
```

Very useful for debugging.

---

### Step 4 – YAML Sent to API Server

Helm sends rendered manifests to:

Kubernetes API Server

Same as if you ran:

```
kubectl apply -f file.yaml
```

---

### Step 5 – Release Object Stored

Helm creates a Secret like:

```
sh.helm.release.v1.myapp.v1
```

This stores:

* Manifest
* Chart metadata
* Values
* Revision number

---

# How Upgrade Works Internally

When you run:

```
helm upgrade myapp ./chart
```

Helm:

1. Gets previous release from Secret
2. Renders new templates
3. Compares
4. Applies changes
5. Stores new revision (v2)

Rollback just re-applies older stored manifest.

That’s why rollback is instant.

---

# Helm Architecture in Enterprise Setup

In real DevOps setups (especially multi-team clusters):

* Helm runs inside CI/CD pipeline
* Charts stored in Artifact repository (e.g., Harbor, Nexus)
* Git stores values files
* GitOps tool like:

    * Argo CD
    * Flux

These tools use Helm as a rendering engine.

Helm becomes the packaging layer.
GitOps becomes deployment controller.

---

# Security Model in Helm v3

Helm uses:

* Your kubeconfig
* Kubernetes RBAC

So if your user has only namespace access,
Helm can only deploy to that namespace.

No cluster-wide privilege escalation.

Much cleaner than Tiller days.

