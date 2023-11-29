# Helm installation

## The operator installation is managed by a helm chart. To install run:

```
helm install flink-kubernetes-operator helm/flink-kubernetes-operator
```
## Alternatively to install the operator (and also the helm chart) to a specific namespace:

```
helm install flink-kubernetes-operator helm/flink-kubernetes-operator --namespace flink --create-namespace
```

