# Why Storage in Kubernetes Is Different

Containers are **ephemeral**.

If a Pod dies:

* Container filesystem is gone
* Data written inside container is lost

So Kubernetes separates:

* **Compute (Pods)**
* **Storage (Volumes)**

This separation is the key.

---

# Volume — Pod-Level Storage

## What is a Volume?

> A Volume is storage attached to a Pod.

It lives **as long as the Pod lives**.

## Example: EmptyDir

```yaml
volumes:
  - name: cache
    emptyDir: { }
```

This:

* Lives only for the pod
* Deleted when pod dies
* Good for temp storage

---

## Volume Types

| Type      | Use                           |
|-----------|-------------------------------|
| emptyDir  | Temporary cache               |
| configMap | Inject config files           |
| secret    | Inject credentials            |
| hostPath  | Local node storage (dev only) |

---

# The Problem with Pod Volumes

If pod restarts on another node:

* emptyDir is lost
* hostPath may not exist
* Data disappears

For databases and persistent data, you need more.

---

# PersistentVolume (PV) — Cluster-Level Storage

## What is a PV?

> A cluster-level storage resource.

It represents real storage:

* AWS EBS
* GCP Persistent Disk
* Azure Disk
* NFS
* Local disk

---

## PV Characteristics

* Independent of Pods
* Exists at cluster level
* Can outlive pods
* Bound to a claim

---

## Example PV (Static Provisioning)

```yaml
apiVersion: v1
kind: PersistentVolume
spec:
  capacity:
    storage: 5Gi
  accessModes:
    - ReadWriteOnce
```

---

# PersistentVolumeClaim (PVC)

Pods don’t use PV directly.

They request storage via a PVC.

> PVC = request for storage

---

## Example PVC

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 5Gi
```

---

## Flow

```
Pod → PVC → PV → Real Storage
```

This decouples:

* Application
* Storage implementation

---

# StorageClass — Dynamic Provisioning

Without StorageClass:

* Admin manually creates PV
* App claims it

With StorageClass:

* PV created automatically

## What is a StorageClass?

> A template for dynamically provisioning storage.

Example:

```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
provisioner: kubernetes.io/aws-ebs
parameters:
  type: gp2
```

Now when PVC is created:

* Kubernetes provisions EBS automatically
* Binds to PVC

No manual PV needed.

---

# Access Modes

| Mode                | Meaning              |
|---------------------|----------------------|
| ReadWriteOnce (RWO) | One node             |
| ReadWriteMany (RWX) | Multiple nodes       |
| ReadOnlyMany (ROX)  | Read-only multi-node |

Example:

* MySQL → RWO
* Shared file storage → RWX

---

# 8Deployment vs StatefulSet Storage

## Deployment + PVC

All replicas may share volume (dangerous).

## StatefulSet + volumeClaimTemplates

Each pod gets its own PVC automatically.

Example:

```yaml
volumeClaimTemplates:
  - metadata:
      name: data
```

Creates:

```
data-mysql-0
data-mysql-1
data-mysql-2
```

Perfect for databases.

---

# Use Cases

| Use Case            | Kubernetes Object |
|---------------------|-------------------|
| Cache               | emptyDir          |
| Spring Boot uploads | PVC               |
| MySQL               | StatefulSet + PVC |
| Kafka               | StatefulSet + PVC |
| NFS shared storage  | RWX StorageClass  |

