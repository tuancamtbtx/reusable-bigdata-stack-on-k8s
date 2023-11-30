## 1. Add the Helm Chart Repository
Use helm repo add command to add the Helm chart repository that contains charts to install Rancher. For more information about the repository choices and which is best for your use case, see Choosing a Rancher Version.
- Latest: Recommended for trying out the newest features
```
helm repo add rancher-latest https://releases.rancher.com/server-charts/latest
```
- Stable: Recommended for production environments
```
helm repo add rancher-stable https://releases.rancher.com/server-charts/stable
```
- Alpha: Experimental preview of upcoming releases.
```
helm repo add rancher-alpha https://releases.rancher.com/server-charts/alpha
```
## 2. Create a Namespace for Rancher
We'll need to define a Kubernetes namespace where the resources created by the Chart should be installed. This should always be cattle-system:

```
kubectl create namespace cattle-system
```