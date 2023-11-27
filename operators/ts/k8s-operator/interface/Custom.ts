import {KubernetesObject} from "@kubernetes/client-node";



export interface SparkCustomResource extends KubernetesObject {
    spec: SparkAppResourceSpec;
    status: SparkAppResourceStatus;
}

export interface SparkAppResourceSpec {
    sparkMaster: string;
    sparkExecutor: string;
}

export interface SparkAppResourceStatus {
    observedGeneration?: number;
}