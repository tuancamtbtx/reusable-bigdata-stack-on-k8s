package org.example.spark.operator.controller;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.cache.Cache;
import io.fabric8.kubernetes.client.informers.cache.Lister;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.example.spark.operator.model.v1alpha1.SparkApp;
import org.example.spark.operator.model.v1alpha1.SparkAppStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tuan.nguyen3
 */
public class SparkAppController {

  public static final String APP_LABEL = "app";
  private static final Logger log = LoggerFactory.getLogger(SparkAppController.class);
  private final BlockingQueue<String> workQueue;
  private final SharedIndexInformer<SparkApp> sparkAppInformer;
  private final SharedIndexInformer<Pod> podInformer;
  private final Lister<SparkApp> sparkAppLister;
  private final Lister<Pod> podLister;
  private final KubernetesClient kubernetesClient;
  private final MixedOperation<SparkApp, KubernetesResourceList<SparkApp>, Resource<SparkApp>> sparkAppClient;

  public SparkAppController(
      SharedIndexInformer<Pod> podInformer,
      SharedIndexInformer<SparkApp> sparkAppInformer,
      KubernetesClient kubernetesClient, MixedOperation<SparkApp, KubernetesResourceList<SparkApp>, Resource<SparkApp>> sparkAppClient,
      String namespace) {
    this.workQueue = new ArrayBlockingQueue<>(1024);;
    this.sparkAppInformer = sparkAppInformer;
    this.podInformer = podInformer;
    this.kubernetesClient = kubernetesClient;
    this.sparkAppClient = sparkAppClient;

    this.podLister = new Lister<>(podInformer.getIndexer(), namespace);
    this.sparkAppLister = new Lister<>(sparkAppInformer.getIndexer(), namespace);
    addEventHandlersToSharedIndexInformers();
  }

  public void run() {
    log.info("Starting SparkAppController");
    while (!Thread.currentThread().isInterrupted()) {
      if (podInformer.hasSynced() && sparkAppInformer.hasSynced()) {
        break;
      }
    }
    while (true) {
      try {
        if (workQueue.isEmpty()) {
          log.info("Work queue is empty, sleeping for 1 second");
        }
        String key = workQueue.take();
        Objects.requireNonNull(key, "Key can not be null");
        log.info("Processing key {}", key);
        if (!(key.contains("/"))) {
          log.warn("Invalid resource key: {}", key);
        }

        String name = key.split("/")[1];
        SparkApp sparkApp = sparkAppLister.get(name);
        if (sparkApp == null) {
          log.error("PodSet {} in workqueue no longer exists", name);
          return;
        }
        reconcile(sparkApp);

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.error("SparkAppController interrupted", e);
      }
    }
  }

  public void reconcile(SparkApp sparkApp) {
    log.info("Reconciling SparkApp {}", sparkApp.getMetadata().getName());
    List<String> pods = podCountByLabel(APP_LABEL, sparkApp.getMetadata().getName());
    if (pods.isEmpty()) {
      createPods(sparkApp.getSpec().getReplicas(), sparkApp);
      return;
    }
    int existingPods = pods.size();

    if (existingPods < sparkApp.getSpec().getReplicas()) {
      createPods(sparkApp.getSpec().getReplicas() - existingPods, sparkApp);
    }
    int diff = existingPods - sparkApp.getSpec().getReplicas();
    for (; diff > 0; diff--) {
      String podName = pods.remove(0);
      kubernetesClient.pods().inNamespace(sparkApp.getMetadata().getNamespace()).withName(podName).delete();
    }
    // update spark app status

    updateAvailableReplicasInSparkAppStatus(sparkApp, sparkApp.getSpec().getReplicas());
  }

  private void updateAvailableReplicasInSparkAppStatus(SparkApp sparkApp, int replicas) {
    SparkAppStatus podSetStatus = new SparkAppStatus();
    podSetStatus.setAvailableReplicas(replicas);
    sparkApp.setStatus(podSetStatus);
    sparkAppClient.inNamespace(sparkApp.getMetadata().getNamespace()).resource(sparkApp).replaceStatus();
  }

  private void createPods(int numberOfPods, SparkApp sparkApp) {
    for (int index = 0; index < numberOfPods; index++) {
      Pod pod = createNewPod(sparkApp);
      pod = kubernetesClient.pods().inNamespace(sparkApp.getMetadata().getNamespace()).resource(pod).create(pod);
      kubernetesClient.pods().inNamespace(sparkApp.getMetadata().getNamespace()).withName(pod.getMetadata().getName())
          .waitUntilCondition(Objects::nonNull, 3, TimeUnit.SECONDS);
    }
    log.info("Created {} pods for {} PodSet", numberOfPods, sparkApp.getMetadata().getName());
  }

  private void addEventHandlersToSharedIndexInformers() {
    sparkAppInformer.addEventHandler(new ResourceEventHandler<SparkApp>() {
      @Override
      public void onAdd(SparkApp sparkApp) {
        log.info("SparkApp added: {}", sparkApp.getMetadata().getName());
        enqueueSparkApp(sparkApp);
      }

      @Override
      public void onUpdate(SparkApp sparkApp, SparkApp newSparkApp) {
        log.info("SparkApp updated: {}", sparkApp.getMetadata().getName());
        enqueueSparkApp(newSparkApp);
      }

      @Override
      public void onDelete(SparkApp sparkApp, boolean b) {

      }
    });
    podInformer.addEventHandler(new ResourceEventHandler<Pod>() {
      @Override
      public void onAdd(Pod pod) {
        handlePodObject(pod);
      }

      @Override
      public void onUpdate(Pod pod, Pod newPod) {
        if (!pod.getMetadata().getResourceVersion().equals(newPod.getMetadata().getResourceVersion())) {
          return;
        }
        handlePodObject(newPod);
      }

      @Override
      public void onDelete(Pod pod, boolean b) {

      }
    });
  }

  private List<String> podCountByLabel(String label, String sparkAppName) {
    List<String> sparkAppNames = new ArrayList<>();
    List<Pod> pods = podLister.list();
    for (Pod pod : pods) {
      if (pod.getMetadata().getLabels().entrySet().contains(new AbstractMap.SimpleEntry<>(label, sparkAppName))) {
        if (pod.getStatus().getPhase().equals("Running") || pod.getStatus().getPhase().equals("Pending")) {
          sparkAppNames.add(pod.getMetadata().getName());
        }
      }
    }

    log.info("count: {}", sparkAppNames.size());
    return sparkAppNames;
  }

  public void enqueueSparkApp(SparkApp sparkApp) {
    log.info("Enqueueing SparkApp {}", sparkApp.getMetadata().getName());
    String key = Cache.metaNamespaceKeyFunc(sparkApp);
    log.info("Going to enqueue key {}", key);
    if (key != null && !key.isEmpty()) {
      log.info("Adding item to workQueue");
      workQueue.add(key);
    }
  }

  private void handlePodObject(Pod pod) {
    log.info("Handling pod object {}", pod.getMetadata().getName());
    OwnerReference ownerReference = getControllerOf(pod);
    if (ownerReference == null || !ownerReference.getKind().equalsIgnoreCase("SparkApp")) {
      log.info("Owner reference is null or not SparkApp");
      return;
    }
    SparkApp sparkApp = sparkAppLister.get(ownerReference.getName());
    log.info("SparkLister returned {} for sparkApp", sparkApp);
    if (sparkApp != null) {
      enqueueSparkApp(sparkApp);
    }
  }


  private Pod createNewPod(SparkApp sparkApp) {
    return new PodBuilder().withNewMetadata().withName(sparkApp.getMetadata().getName())
        .withLabels(Collections.singletonMap(APP_LABEL, sparkApp.getMetadata().getName())).addNewOwnerReference().withController(true)
        .withKind("SparkApp").withApiVersion("sparkoperator.k8s.io/v1alpha1").withName(sparkApp.getMetadata().getName())
        .withUid(sparkApp.getMetadata().getUid()).endOwnerReference().endMetadata().withNewSpec().addNewContainer().withName("spark-app")
        .withImage("busybox").withCommand("sleep", "3600").endContainer().endSpec().build();
  }

  private OwnerReference getControllerOf(Pod pod) {
    List<OwnerReference> ownerReferences = pod.getMetadata().getOwnerReferences();
    if (ownerReferences != null && !ownerReferences.isEmpty()) {
      for (OwnerReference ownerReference : ownerReferences) {
        if (ownerReference.getController() != null && ownerReference.getController()) {
          return ownerReference;
        }
      }
    }
    return null;
  }
}
