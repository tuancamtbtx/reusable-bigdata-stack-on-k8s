# This is the provider configuration
provider "rancher2" {
    api_url = "http://your-rancher-server-url"
    access_key = var.access_key
    secret_key = var.secret_key
}

# This is the definition of your cluster
resource "rancher2_cluster" "my_cluster" {
    name = "my_cluster"
    description = "My awesome Kubernetes cluster managed by Rancher"
    rke_config {
        network {
            plugin = "canal"
        }
    }
}

output "cluster_id" {
    value = rancher2_cluster.my_cluster.id
    description = "ID of the created Rancher2 cluster"
}
