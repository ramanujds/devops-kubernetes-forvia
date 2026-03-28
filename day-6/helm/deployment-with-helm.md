## Blue/Green Deployment with Helm

## What is Blue/Green?

Blue/Green means:

* **Blue** = currently live version
* **Green** = new version
* You switch traffic from Blue → Green instantly
* If something breaks, switch back

Zero downtime. Instant rollback.

---

# How to Implement Blue/Green with Helm

There are 3 common approaches.

---

# Approach 1 — Two Deployments + One Service (Simplest)

You create:

* Deployment: myapp-blue
* Deployment: myapp-green
* One Service

The Service selector decides which one gets traffic.

Example:

Blue deployment labels:

```yaml
labels:
  app: myapp
  version: blue
```

Green deployment:

```yaml
labels:
  app: myapp
  version: green
```

Service:

```yaml
selector:
  app: myapp
  version: blue
```

To switch traffic:

You just update Service selector to green.

With Helm:

You parameterize version in values.yaml:

```yaml
activeColor: blue
```

Then in Service template:

```yaml
version: { { .Values.activeColor } }
```

Deploy new green version:

```
helm upgrade myapp ./chart --set activeColor=green
```

Traffic instantly switches.

---

# Approach 2 — Two Separate Helm Releases

You install:

```
helm install myapp-blue ./chart
helm install myapp-green ./chart
```

Each release runs independently.

Your Ingress or LoadBalancer routes traffic to one.

To switch:
Change Ingress backend.

Cleaner separation.
More control.
More resources used.

---

# Approach 3 — Use Ingress / Service Mesh (Advanced)

Instead of switching Service selector, you control traffic at:

* NGINX Ingress Controller
* Istio
* Linkerd

Ingress example:
Route 100% traffic to green.

Or gradually shift traffic (which becomes Canary strategy).

Helm just templates these configs.

---

# Where Helm Actually Helps

Helm gives you:

1. Parameterization
2. Versioning
3. Easy switching via values.yaml
4. Rollback support

Instead of manually editing YAML,
you just change a value.

That’s clean.

---

# Example Real Flow (Production Style)

Let’s say:

Current live = blue (image v1)

You deploy green:

```
helm upgrade myapp ./chart --set image.tag=v2
```

But you don’t switch traffic yet.

After validation:

```
helm upgrade myapp ./chart --set activeColor=green
```

Traffic switches instantly.

If bug found:

```
helm upgrade myapp ./chart --set activeColor=blue
```

Instant rollback.

Very powerful.

---

# Important Limitation

Helm itself does NOT:

* Automatically manage traffic shifting
* Monitor health for switching
* Provide progressive delivery logic

For that, you use tools like:

* Argo Rollouts
* Flagger

They integrate with Helm charts.

Helm handles packaging.
These tools handle strategy.

---

# Blue/Green vs Canary with Helm

Blue/Green:
Switch 100% traffic instantly.

Canary:
Shift gradually (10% → 50% → 100%)

Helm can support both via templating,
but advanced automation needs extra controllers.

---



