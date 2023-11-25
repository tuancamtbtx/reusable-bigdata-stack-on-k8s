import * as FS from 'fs';
import * as k8s from '@kubernetes/client-node';
import * as https from 'https';
import {
    KubernetesObject,
    loadYaml,
    V1CustomResourceDefinition,
    V1CustomResourceDefinitionVersion,
    Watch,
} from '@kubernetes/client-node';

export interface SparkApplicationSpec { 
    
}
export interface SparkApplication {
    apiVersion: string;
    kind: string;
    metadata: k8s.V1ObjectMeta;
    spec: SparkApplicationSpec;
}