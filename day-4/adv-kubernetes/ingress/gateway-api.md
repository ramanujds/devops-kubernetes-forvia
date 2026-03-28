# What is Ingress?

Ingress is:

* A Kubernetes resource
* Used to expose HTTP/HTTPS services
* Works with an Ingress Controller (like NGINX)

It allows:

* Host-based routing
* Path-based routing
* TLS termination

Simple example:

```yaml
kind: Ingress
spec:
  rules:
    - host: myapp.local
      http:
        paths:
          - path: /api
            backend:
              service:
                name: api-service
                port:
                  number: 80
```

Problem?

Ingress is:

* Limited
* Not very extensible
* Controller-specific behavior via annotations
* Hard to standardize across vendors

---

# Why Gateway API Was Introduced?

Kubernetes SIG-Network introduced Gateway API to:

* Replace Ingress
* Make routing more expressive
* Support TCP, UDP, gRPC
* Improve multi-team separation
* Reduce annotation mess

Gateway API = CRDs (Custom Resource Definitions)

It is more modular and role-based.

---

# Core Concept Difference

### Ingress Model

Single resource does everything.

```
Ingress
   ↓
Controller
   ↓
Service
```

Everything (routing + listener config) is inside one object.

---

### Gateway API Model

Split responsibilities into multiple objects:

```
GatewayClass → Gateway → HTTPRoute → Service
```

More modular. More enterprise friendly.

---

# Gateway API Components Explained

## GatewayClass

Defines which controller manages it.

Example:

* NGINX Gateway Fabric
* Istio
* Kong Gateway

---

## Gateway

Defines:

* Listener (port 80, 443)
* TLS configuration
* Which routes can attach

Example:

```yaml
kind: Gateway
spec:
  listeners:
    - name: http
      port: 80
      protocol: HTTP
```

Think of this as:
Load Balancer + Listener configuration

---

## HTTPRoute

Defines:

* Path rules
* Host rules
* Backend services

Example:

```yaml
kind: HTTPRoute
spec:
  parentRefs:
    - name: my-gateway
  rules:
    - matches:
        - path:
            type: PathPrefix
            value: /api
      backendRefs:
        - name: api-service
          port: 80
```

Now routing logic is separated from infra configuration.

---

# Architectural Comparison

### Ingress Architecture

```
Client
   ↓
Ingress
   ↓
Ingress Controller
   ↓
Service
```

Everything bundled together.

---

### Gateway API Architecture

```
Client
   ↓
Gateway (listener)
   ↓
HTTPRoute
   ↓
Service
```

More separation of concerns.

---

---

# Real DevOps Perspective

In a company:

### With Ingress

Dev team defines:

* TLS
* Routing
* Hostnames
* Controller-specific annotations

Security team not happy.

---

### With Gateway API

Infra team controls:

* Gateway
* TLS certificates
* Ports

Dev team controls:

* HTTPRoute
* Application-level routing

Cleaner separation.

This is huge in enterprise.

---

# Example: Multi-Team Scenario

Infra Team creates:

```yaml
Gateway:
  listeners:
    - port: 443
      protocol: HTTPS
```

App Team creates:

```yaml
HTTPRoute:
  matches:
    - path: /payments
```

No need to touch TLS or load balancer.

---

# Should You Stop Using Ingress?

Not immediately.

Reality:

* Ingress is stable
* Widely supported
* Used everywhere (EKS, GKE, AKS)

Gateway API adoption is growing but not universal yet.

Think of it like:

Ingress = Spring MVC
Gateway API = Spring WebFlux + modular architecture

Both valid. One more modern.

---

# When To Use What?

Use Ingress if:

* Small project
* Docker Desktop
* Basic HTTP routing
* Learning Kubernetes

Use Gateway API if:

* Enterprise setup
* Multi-team platform
* Need TCP/UDP routing
* Want cleaner separation
* Planning long-term architecture

