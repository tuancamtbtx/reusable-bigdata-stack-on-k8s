package org.example.spark.operator.model.v1alpha1;

/**
 * @author tuan.nguyen3
 */
public class SparkAppSpec {
  private int replicas;
  public int getReplicas() {
    return replicas;
  }

  @Override
  public String toString() {
    return "PodSetSpec{replicas=" + replicas + "}";
  }

  public void setReplicas(int replicas) {
    this.replicas = replicas;
  }
}
