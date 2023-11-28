## Running Trino using Helm#
Run the following commands from the system with helm and kubectl installed and configured to connect to your running Kubernetes cluster:

1. Validate kubectl is pointing to the correct cluster by running the command:
```
kubectl cluster-info
```
You should see output that shows the correct Kubernetes control plane address.

2.Add the Trino Helm chart repository to Helm if you haven’t done so already. This tells Helm where to find the Trino charts. You can name the repository whatever you want, trino is a good choice.
```
helm repo add trino https://trinodb.github.io/charts
```

3. Install Trino on the Kubernetes cluster using the Helm chart. Start by running the install command to use all default values and create a cluster called example-trino-cluster.

```
helm install example-trino-cluster trino/trino
```

This generates the Kubernetes configuration files by inserting properties into helm templates. The Helm chart contains default values that can be overridden by a YAML file to update default settings.

```
helm install -f example.yaml example-trino-cluster trino/trino

```