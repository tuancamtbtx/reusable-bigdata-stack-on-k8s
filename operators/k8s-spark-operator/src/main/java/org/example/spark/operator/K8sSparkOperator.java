package org.example.spark.operator;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.example.spark.operator.controller.SparkAppController;
import org.example.spark.operator.model.v1alpha1.SparkApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tuan.nguyen3
 */
public class K8sSparkOperator {
  private static final Logger log = LoggerFactory.getLogger(K8sSparkOperator.class);

  public static void main(String[] args) throws IOException {
    log.info("K8s Spark Operator starting!");
    try (KubernetesClient client = new KubernetesClientBuilder().build()){
      String namespace = client.getNamespace();
      if (namespace == null) {
        log.info("No namespace found via config, assuming default.");
        namespace = "default";
      }
      log.info("Using namespace : " + namespace);
      SharedInformerFactory informerFactory = client.informers();
      MixedOperation<SparkApp, KubernetesResourceList<SparkApp>, Resource<SparkApp>> sparkAppClient = client.resources(SparkApp.class);
      SharedIndexInformer<Pod> podSharedIndexInformer = informerFactory.sharedIndexInformerFor(Pod.class, 10 * 60 * 1000L);
      SharedIndexInformer<SparkApp> sparkAppSharedIndexInformer = informerFactory.sharedIndexInformerFor(SparkApp.class, 10 * 60 * 1000L);
      SparkAppController podSetController = new SparkAppController(podSharedIndexInformer, sparkAppSharedIndexInformer,client, sparkAppClient, namespace);
      Future<Void> startedInformersFuture = informerFactory.startAllRegisteredInformers();
      startedInformersFuture.get();

      podSetController.run();

    }
    catch (KubernetesClientException | ExecutionException exception){
      log.error("K8s Spark Operator failed to start!", exception);
    }
    catch (InterruptedException e) {
      log.error("K8s Spark Operator failed to start!", e);
      Thread.currentThread().interrupt();
    }
  }
}
