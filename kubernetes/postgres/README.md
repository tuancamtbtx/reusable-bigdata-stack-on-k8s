# Deploying  Postgres On a Kubernetes Cluster
### After the Configmap has been defined, it can be created with the command below:

```
kubectl apply -f postgres-configmap.yaml -n airflow 
```
### After the YAML file has been defined, the resources can be created with the command below:
```
kubectl apply -f postgres-storage.yaml -n airflow 
```
### Once done, check that they were created successfully:
```
kubectl get pv -n airflow 
kubectl get pvc -n airflow 
```
### To create the Postgres deployment, we can use the following command:
```
kubectl apply -f postgres-deployment.yaml -n airflow
```
Once again, check that the deployment has been created successfully:

```
kubectl get pods -n airflow 
```
### As defined above, the Postgres service will listen on port 5432 and it will be accessible from inside the cluster via the same port. This port will be used when configuring the Airflow scheduler. We create the Postgres service with the same command:
```
kubectl apply -f postgres-service -n airflow 
```

### Or Run all with Kustomize
```
kustomize build .  | kubectl apply -f -
```