# ArgoCD Cluster Setup Notes

## 1) Log in to ArgoCD CLI first

If you see `Argo CD server address unspecified`, the CLI is not logged in.

```bash
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

In another terminal:

```bash
kubectl get secret argocd-initial-admin-secret -n argocd -o jsonpath="{.data.password}" | base64 -d && echo
argocd login localhost:8080 --username admin --password <PASSWORD> --insecure
```

## 2) Add cluster to ArgoCD

```bash
kubectl config get-contexts
argocd cluster add <cluster-context-name>
argocd cluster list
```

The destination `server` in Application manifests must exactly match the cluster URL shown by:

```bash
argocd cluster list
```

## 3) URL mismatch troubleshooting

If app manifests fail due to destination cluster mismatch:

1. Check current kube API server URL:

```bash
kubectl config view --minify -o jsonpath='{.clusters[0].cluster.server}'
```

2. Ensure every app manifest `spec.destination.server` uses the exact same value.
3. Remove stale ArgoCD cluster entries and re-add if needed:

```bash
argocd cluster rm <old-server-url>
argocd cluster add <cluster-context-name>
```

## 4) GKE-specific fix (managed namespace restriction)

If `argocd cluster add` fails with forbidden error creating service account in `kube-system`:

```bash
kubectl --context gke_my-training-projects-466011_us-central1_my-gke-cluster create namespace argocd-manager
argocd cluster add gke_my-training-projects-466011_us-central1_my-gke-cluster --system-namespace argocd-manager
```

Cause: GKE Warden/managed namespace policy can deny writes to `kube-system`.

## 5) If RBAC still fails on GKE

Grant cluster-admin (or ask org admin to grant equivalent permissions):

```bash
kubectl --context gke_my-training-projects-466011_us-central1_my-gke-cluster create clusterrolebinding ramanujds9-cluster-admin --clusterrole=cluster-admin --user=ramanujds9@gmail.com
```

Then retry:

```bash
argocd cluster add gke_my-training-projects-466011_us-central1_my-gke-cluster --system-namespace argocd-manager
argocd cluster list
```

## 6) Post-setup checks

- Ensure target app namespaces exist (for example `prod`) unless using `CreateNamespace=true`.
- For any Helm rendering errors in ArgoCD, confirm required chart files (for example `_helpers.tpl`) are committed and pushed to the Git branch referenced by `targetRevision`.

