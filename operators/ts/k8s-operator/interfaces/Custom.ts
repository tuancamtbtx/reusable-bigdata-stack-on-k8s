import {KubernetesObject} from "@kubernetes/client-node";

export interface MyCustomResource extends KubernetesObject {
    spec: MyCustomResourceSpec;
    status: MyCustomResourceStatus;
}

export interface MyCustomResourceSpec {
    foo: string;
    bar?: number;
}

export interface MyCustomResourceStatus {
    observedGeneration?: number;
}