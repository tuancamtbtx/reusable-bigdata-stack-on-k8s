package org.example.spark.operator.model.v1alpha1;

/**
 * @author tuan.nguyen3
 */
public class SparkAppStatus {
  public int getAvailableReplicas() {
    return availableReplicas;
  }

  public void setAvailableReplicas(int availableReplicas) {
    this.availableReplicas = availableReplicas;
  }

  @Override
  public String toString() {
    return "PodSetStatus{ availableReplicas=" + availableReplicas + "}";
  }

  private int availableReplicas;
}
