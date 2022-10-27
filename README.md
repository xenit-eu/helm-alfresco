# Xenit Alfresco Helm Chart 
[![Latest version of 'alfresco' @ Cloudsmith](https://api-prd.cloudsmith.io/v1/badges/version/xenit/open-source/helm/alfresco/latest/x/?render=true&show_latest=true)](https://cloudsmith.io/~xenit/repos/open-source/packages/detail/helm/alfresco/latest/)

This is a helm chart for installing Alfresco

## Helm
[![Hosted By: Cloudsmith](https://img.shields.io/badge/OSS%20hosting%20by-cloudsmith-blue?logo=cloudsmith&style=for-the-badge)](https://cloudsmith.com)

Package repository hosting is graciously provided by  [Cloudsmith](https://cloudsmith.com).
Cloudsmith is the only fully hosted, cloud-native, universal package management solution, that
enables your organization to create, store and share packages in any format, to any place, with total
confidence.

You can install this helm chart on you K8s cluster. Keep in mind that you will need to add some `--set` statements for this to work:
```bash
helm install alfresco \
  --repo 'https://repo.xenit.eu/public/open-source/helm/charts/'
```

Or you can use it as a dependency in your `requirements.yaml` in your own chart.
```yaml
dependencies:
  - name: alfresco
    version: 0.1.2
    repository: https://repo.xenit.eu/public/open-source/helm/charts/
```



## Dev Requirements
Make sure you have the following installed:
* Kubectl: https://kubernetes.io/docs/tasks/tools/#kubectl
* docker: https://www.docker.com/get-started/
* Helm: https://helm.sh/docs/intro/install/
* kind: https://kind.sigs.k8s.io/docs/user/quick-start/
* skaffold: https://skaffold.dev/docs/install/

## Start Local Cluster
* To start the cluster you have to create one using kind with the config file as a parameter that is under the directory kind: 
  ```bash
  kind create cluster --config=kind/config.yaml
  ```
* switch to kind-kind context :
  ```bash
  kubectl config use-context kind-kind
  ```
* Add An ingress controller by running this command after starting the cluster:
  ```bash
  kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
  ```

* set up the credentials in local_values.yaml for LDAP and alfresco
* wait for the ingress controller to be ready you can check by running this command :
  ```bash
  kubectl wait --namespace ingress-nginx   --for=condition=ready pod   --selector=app.kubernetes.io/component=controller  --timeout=90s
  ```
* some the services are disabled by default to minimize the resource usage such as :
  - solr
  - transformServices
  - digitalWorkspace
  
  to enable them modify the values inside local-values.yaml
* finally, run skaffold (instead of helm) and wait for the ingress controller to be ready first: 
  ```bash
  skaffold dev
  ``` 
## Image Requirements
This helm chart supports a lot of features like share and desktop sync. You are however yourself responsible to provide an ACS image with the correct amps installed to support these features.
Please note that this helm chart is build to support the xenit open source images. These are build on the official Alfresco Images but have additional K8S support.
The deployments that rely on Xenit Images are the following:
* acs
* share
* postgresql
* solr

For more information take a look at 

* https://hub.docker.com/u/xenit
* https://github.com/xenit-eu


## Configuration

### General

#### `general.strategy`

* Required: false
* Default: 
  ```yaml
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 1
    maxUnavailable: 0
  ```
* Description: You can overwrite here the rollout strategy of deployments. This will be effective on ALL deployments in the helm chart that have strategy type RollingUpdate (default)

#### `general.podAnnotations`

* Required: false
* Default: None
* Example: 
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to ALL deployments and statefullSets

#### `general.imagePullSecrets`

* Required: false
* Default: None
* Example: 
  ```yaml
    - name: privateDockerRepo1Secret
    - name: privateDockerRepo2Secret
  ```
* Description: If you use an image that is hosted on a private repo besides the xenit repo or the quay alfresco repo you can create secrets on your cluster and reference them here. The secrets will be referenced in all Deployments and StatefullSets.

#### `general.serviceType`

* Required: false
* Default: None
* Description: will set a serviceType on the services that are exposed via an ingress. This might be useful for example when you are working on AWS infra with an AWS ALB which requires NodePort services

#### `general.db.username`

* Required: false
* Default: None
* Description: Used in the ACS and SyncService pod to access the Database and to set the username of the rootuser of the postgres (if enabled)
* Note: If not specified the helm chart will try to reuse the value used in previous deployments. If these are not there a random user will be used.

#### `general.db.password`

* Required: false
* Default: None
* Description: Used in the ACS and SyncService pod to access the Database and to set the password of the rootuser of the postgres (if enabled)
* Note: If not specified the helm chart will try to reuse the value used in previous deployments. If these are not there a random password will be used.

#### `general.networkPolicies.enabled`

* Required: false
* Default: true
* Description: A field to enabled/disable network policies.

#### `general.cni`

* Required: false
* Default: cilium
* Description: A field to tell the helm chart what cni provider your cluster is using. By default we assume cilium. If this is not the case you will need to add a network policy to allow the following
* Alfresco to access heartbeat

#### `general.secrets.acs.selfManaged`

* Required: false
* Default: false
* Description: Whether or not you want to provide secrets for the helm chart yourself. This is useful when working on a prod environment and you want a secure secret solution (for example Bitnami' Sealed secrets)
* Please note that when you enable this you are yourself responsible to provide a secret acs-secret in the namespace that you will install this chart in.
* Secret data expected:
```
  GLOBAL_objectstorage.store.myS3ContentStore.value.accessKey
  GLOBAL_objectstorage.store.myS3ContentStore.value.secretKey
```

#### `general.secrets.mq.selfManaged`

* Required: false
* Default: false
* Description: Whether or not you want to provide secrets for the helm chart yourself. This is useful when working on a prod environment and you want a secure secret solution (for example Bitnami' Sealed secrets)
* Please note that when you enable this you are yourself responsible to provide a secret mq-secret in the namespace that you will install this chart in.
* Secret data expected:
```
  ACTIVEMQ_ADMIN_LOGIN
  ACTIVEMQ_ADMIN_PASSWORD
  GLOBAL_messaging.broker.username
  GLOBAL_messaging.broker.password
```

#### `general.secrets.db.selfManaged`

* Required: false
* Default: false
* Description: Whether or not you want to provide secrets for the helm chart yourself. This is useful when working on a prod environment and you want a secure secret solution (for example Bitnami' Sealed secrets)
* Please note that when you enable this you are yourself responsible to provide a secret db-secret in the namespace that you will install this chart in.
* Secret data expected:
```
  DB_USERNAME
  DB_PASSWORD  
  POSTGRES_USER
  POSTGRES_PASSWORD
```

#### `general.secrets.imageCredentials.selfManaged`

* Required: false
* Default: false
* Description: Whether or not you want to provide secrets for the helm chart yourself. This is useful when working on a prod environment and you want a secure secret solution (for example Bitnami' Sealed secrets)
* Please note that when you enable this you are yourself responsible to provide a secret privatecred alfrescocred in the namespace that you will install this chart in.
* Secret data expected: Both secrets should be dockerconfigjson secrets

### Ingress

#### `ingress.host`

* Required: true
* Default: None
* Description: The host that points to the alfresco cluster for all services besides the syncService service

#### `ingress.syncServiceHost`

* Required: when `syncService.enabled` is `true`
* Default: None
* Description: The host that points to the alfresco cluster for the syncService service

#### `ingress.ingressAnnotations`

* Required: false
* Default:
  ```
  kubernetes.io/ingress.class: "nginx"
  cert-manager.io/cluster-issuer: "letsencrypt-production"
  ```
* Description: Annotations for ingress


#### `ingress.additionalPaths`

* Required: false
* Default: None
* Example:
```yaml
- path: /service-path
  pathType: Prefix
  backend:
    service:
      name: service-name
        port:
          number: service-port
```
* Description: used to add more path to ingress under the same host name for new services
### ACS

#### `acs.replicaCount`

* Required: false
* Default: `2`
* Description: The number of pods that will be running

#### `acs.image`

* Required: true
* Default: xenit/alfresco-repository-community:7.2.0
* Description: The repository of the docker image that will be used

#### `acs.imagePullPolicy`

* Required: false
* Default: `IfNotPresent`
* Description: Specify when the pods should pull the image from the repositories

#### `acs.livenessProbe.failureThreshold`

* Required: false
* Default: 1
* Description: Specify the livenessProbe failure thresh hold fp how many consecutive failure before it stops probing

#### `acs.livenessProbe.initialDelaySeconds`

* Required: false
* Default: 130
* Description: Specify the livenessProbe initial delay before it starts probing

#### `acs.livenessProbe.periodSeconds`

* Required: false
* Default: 20
* Description: Specify the livenessProbe period between probes

#### `acs.livenessProbe.successThreshold`

* Required: false
* Default: 1
* Description: Specify the livenessProbe success thresh hold for how many consecutive successes for the probe to be considered successful after having failed

#### `acs.livenessProbe.timeoutSeconds`

* Required: false
* Default: 10
* Description: Specify the livenessProbe timeout for probes to be considered as failure

#### `acs.readinessProbe.failureThreshold`

* Required: false
* Default: 6
* Description: Specify the readinessProbe failure thresh hold fp how many consecutive failure before it stops probing

#### `acs.readinessProbe.initialDelaySeconds`

* Required: false
* Default: 60
* Description: Specify the readinessProbe initial delay before it starts probing

#### `acs.readinessProbe.periodSeconds`

* Required: false
* Default: 20
* Description: Specify the readinessProbe period between probes

#### `acs.readinessProbe.successThreshold`

* Required: false
* Default: 1
* Description: Specify the readinessProbe success thresh hold for how many consecutive successes for the probe to be considered successful after having failed

#### `acs.readinessProbe.timeoutSeconds`

* Required: false
* Default: 10
* Description: Specify the readinessProbe timeout for probes to be considered as failure

#### `acs.strategy.type`

* Required: false
* Default: `RollingUpdate`
* Description: Can be set to `Recreate` if you want all your pods to be killed before new ones are created

#### `acs.dbUrl`

* Required: false
* Default: `jdbc:postgresql://postgresql:5432/alfresco`
* Description: Must be overwritten to point to your DB if you are not using the provided postgresql

#### `acs.dbDriver`

* Required: false
* Default: `org.postgresql.Driver`
* Description: If you use another kind of DB then postgres you must specify the driver that needs to be used here

#### `acs.sharePort`

* Required: false
* Default: `443`
* Description: Set to overwrite the share port

#### `acs.shareProtocol`

* Required: false
* Default: `https`
* Description: Set to overwrite the share protocol

#### `acs.additionalEnvironmentVariables`

* Required: false
* Default: None
* Example:
  ```yaml
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `acs.envFrom`

* Required: false
* Default: None
* Description: This allows you to add to the acs-container envFrom section. This was added to allow to integrate secrets that are not added by this helm chart.
* Example:
```yaml
- secretRef:
    name: s3-secret
```

#### `acs.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the acs deployment

#### `acs.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

#### `acs.resources.requests`

* Required: false
* Default:
  ```yaml
  requests:
    memory: "2Gi"
    cpu: "2"
  ```
* Description: The resources a node should keep reserved for your pod
* 
#### `acs.resources.limits`

* Required: false
* Default: None
* Description: The maximum resources a pod may consume from a node

### Digital Workspace

#### `digitalWorkspace.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the digital workspace

#### `digitalWorkspace.replicaCount`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `digitalWorkspace.image`

* Required: true
* Default: `quay.io/alfresco/alfresco-digital-workspace:2.4.2-adw`
* Description: The repository of the docker image that will be used

#### `digitalWorkspace.imagePullPolicy`

* Required: false
* Default: `IfNotPresent`
* Description: Specify when the pods should pull the image from the repositories

#### `digitalWorkspace.strategy.type`

* Required: false
* Default: `RollingUpdate`
* Description: Can be set to `Recreate` if you want all your pods to be killed before new ones are created

#### `digitalWorkspace.basePath`

* Required: false
* Default: `/workspace`
* Description: Specify the basepath where the digital workspace can be reached

#### `digitalWorkspace.additionalEnvironmentVariables`

* Required: false
* Default: None
* Example:
  ```yaml
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `digitalWorkspace.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Digital Workspace deployment

#### `digitalWorkspace.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

#### `digitalWorkspace.resources.requests`

* Required: false
* Default:
  ```yaml
  requests:
    memory: "256Mi"
    cpu: "150m"
  ```
* Description: The resources a node should keep reserved for your pod
*
#### `digitalWorkspace.resources.limits`

* Required: false
* Default: None
* Description: The maximum resources a pod may consume from a node

### Share

#### `share.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the share

#### `share.replicaCount`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `share.image`

* Required: true
* Default: `xenit/alfresco-share-community:7.0`
* Description: The repository of the docker image that will be used

#### `share.imagePullPolicy`

* Required: false
* Default: `IfNotPresent`
* Description: Specify when the pods should pull the image from the repositories

#### `share.strategy.type`

* Required: false
* Default: `RollingUpdate`
* Description: Can be set to `Recreate` if you want all your pods to be killed before new ones are created

#### `share.additionalEnvironmentVariables`

* Required: false
* Default: None
* Example:
  ```yaml
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `share.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Share deployment

#### `share.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

#### `share.resources.requests`

* Required: false
* Default:
  ```yaml
  requests:
    memory: "256Mi"
    cpu: "0.5"
  ```
* Description: The resources a node should keep reserved for your pod
*
#### `share.resources.limits`

* Required: false
* Default: None
* Description: The maximum resources a pod may consume from a node

### Active MQ

#### `mq.adminLogin`

* Required: false
* Default: None
* Description: Sets the username of the admin user of the MQ
* Note: If not specified the helm chart will try to reuse the value used in previous deployments. If these are not there a random login will be used.

#### `mq.adminPassword`

* Required: false
* Default: None
* Description: Sets the password of the admin user of the MQ
* Note: If not specified the helm chart will try to reuse the value used in previous deployments. If these are not there a random password will be used.

#### `mq.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the Active MQ

#### `mq.replicaCount`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `mq.image`

* Required: true
* Default: `alfresco/alfresco-activemq:5.16.1`
* Description: The repository of the docker image that will be used

#### `mq.imagePullPolicy`

* Required: false
* Default: `IfNotPresent`
* Description: Specify when the pods should pull the image from the repositories

#### `mq.strategy.type`

* Required: false
* Default: `Recreate`
* Description: Can be set to `RollingUpdate` if you want to create pod before killing existing pod

#### `mq.additionalEnvironmentVariables`

* Required: false
* Default: None
* Example:
  ```yaml
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `mq.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Active MQ deployment

#### `mq.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

#### `mq.resources.requests`

* Required: false
* Default:
  ```yaml
  requests:
    memory: "512Mi"
    cpu: "0.5"
  ```
* Description: The resources a node should keep reserved for your pod
*
#### `mq.resources.limits`

* Required: false
* Default: None
* Description: The maximum resources a pod may consume from a node

### PostgresQl

#### `postgresql.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the PostgresQl

#### `postgresql.replicaCount`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `postgresql.image`

* Required: true
* Default: `docker.io/xenit/postgres:9.6.23`
* Description: The repository of the docker image that will be used

#### `postgresql.imagePullPolicy`

* Required: false
* Default: `IfNotPresent`
* Description: Specify when the pods should pull the image from the repositories

#### `postgresql.strategy.type`

* Required: false
* Default: `RollingUpdate`
* Description: Can be set to `Recreate` if you want all your pods to be killed before new ones are created

#### `postgresql.additionalEnvironmentVariables`

* Required: false
* Default: None
* Example:
  ```yaml
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `postgresql.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the PostgresQl deployment

#### `postgresql.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

#### `postgresql.resources.requests`

* Required: false
* Default:
  ```yaml
  requests:
    memory: "1Gi"
    cpu: "1"
  ```
* Description: The resources a node should keep reserved for your pod

#### `postgresql.resources.limits`

* Required: false
* Default: None
* Description: The maximum resources a pod may consume from a node

### SOLR

#### `solr.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the Solr

#### `solr.replicaCount`

* Required: false
* Default: `2`
* Description: The number of pods that will be running

#### `solr.image`

* Required: true
* Default: `hub.xenit.eu/alfresco-enterprise/alfresco-solr6:2.0.2`
* Description: The repository of the docker image that will be used

#### `solr.imagePullPolicy`

* Required: false
* Default: `IfNotPresent`
* Description: Specify when the pods should pull the image from the repositories

#### `solr.strategy.type`

* Required: false
* Default: `RollingUpdate`
* Description: Can be set to `OnDelete` if you want your statefulSet controller to no automatically update the pods

#### `solr.additionalEnvironmentVariables`

* Required: false
* Default: None
* Example:
  ```yaml
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `solr.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Solr deployment

#### `solr.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

#### `solr.resources.requests`

* Required: false
* Default:
  ```yaml
  requests:
    memory: "4Gi"
    cpu: "1"
  ```
* Description: The resources a node should keep reserved for your pod
*
#### `solr.resources.limits`

* Required: false
* Default: None
* Description: The maximum resources a pod may consume from a node

### Transform Services

#### `transformServices.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the Transform Services

### Shared File Store

#### `transformServices.sharedFileStore.replicaCount`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `transformServices.sharedFileStore.image`

* Required: true
* Default: `quay.io/alfresco/alfresco-shared-file-store:0.16.0`
* Description: The repository of the docker image that will be used

#### `transformServices.sharedFileStore.imagePullPolicy`

* Required: false
* Default: `IfNotPresent`
* Description: Specify when the pods should pull the image from the repositories

#### `transformServices.sharedFileStore.strategy.type`

* Required: false
* Default: `RollingUpdate`
* Description: Can be set to `Recreate` if you want all your pods to be killed before new ones are created

#### `transformServices.sharedFileStore.additionalEnvironmentVariables`

* Required: false
* Default: None
* Example:
  ```yaml
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `transformServices.sharedFileStore.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Transform Services deployment

#### `transformServices.sharedFileStore.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

#### `transformServices.sharedFileStore.resources.requests`

* Required: false
* Default:
  ```yaml
  requests:
    memory: "512Mi"
    cpu: "200mi"
  ```
* Description: The resources a node should keep reserved for your pod

#### `transformServices.sharedFileStore.resources.limits`

* Required: false
* Default: None
* Description: The maximum resources a pod may consume from a node

### Transform Core All In One

#### `transformServices.transformCoreAio.replicaCount`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `transformServices.transformCoreAio.image`

* Required: true
* Default: `alfresco/alfresco-transform-core-aio`
* Description: The repository of the docker image that will be used

#### `transformServices.transformCoreAio.imagePullPolicy`

* Required: false
* Default: `IfNotPresent`
* Description: Specify when the pods should pull the image from the repositories

#### `transformServices.transformCoreAio.strategy.type`

* Required: false
* Default: `RollingUpdate`
* Description: Can be set to `Recreate` if you want all your pods to be killed before new ones are created

#### `transformServices.transformCoreAio.additionalEnvironmentVariables`

* Required: false
* Default: None
* Example:
  ```yaml
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `transformServices.transformCoreAio.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Transform Core All In One deployment

#### `transformServices.transformCoreAio.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

#### `transformServices.transformCoreAio.resources.requests`

* Required: false
* Default:
  ```yaml
  requests:
    memory: "256Mi"
    cpu: "150m"
  ```
* Description: The resources a node should keep reserved for your pod
*
#### `transformServices.transformCoreAio.resources.limits`

* Required: false
* Default: None
* Description: The maximum resources a pod may consume from a node

### Transform Router

#### `transformServices.transformRouter.replicaCount`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `transformServices.transformRouter.image`

* Required: true
* Default: `quay.io/alfresco/alfresco-transform-router:1.5.2`
* Description: The repository of the docker image that will be used

#### `transformServices.transformRouter.imagePullPolicy`

* Required: false
* Default: `IfNotPresent`
* Description: Specify when the pods should pull the image from the repositories

#### `transformServices.transformRouter.strategy.type`

* Required: false
* Default: `RollingUpdate`
* Description: Can be set to `Recreate` if you want all your pods to be killed before new ones are created

#### `transformServices.transformRouter.additionalEnvironmentVariables`

* Required: false
* Default: None
* Example:
  ```yaml
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `transformServices.transformRouter.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Transform Router deployment

#### `transformServices.transformRouter.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

#### `transformServices.transformRouter.resources.requests`

* Required: false
* Default:
  ```yaml
  requests:
    memory: "128Mi"
    cpu: "100m"
  ```
* Description: The resources a node should keep reserved for your pod
*
#### `transformServices.transformRouter.resources.limits`

* Required: false
* Default: None
* Description: The maximum resources a pod may consume from a node

### Sync Service

#### `syncService.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the Sync Service

#### `syncService.replicaCount`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `syncService.image`

* Required: true
* Default: `quay.io/alfresco/service-sync:3.4.0`
* Description: The repository of the docker image that will be used

#### `syncService.imagePullPolicy`

* Required: false
* Default: `IfNotPresent`
* Description: Specify when the pods should pull the image from the repositories

#### `syncService.strategy.type`

* Required: false
* Default: `RollingUpdate`
* Description: Can be set to `Recreate` if you want all your pods to be killed before new ones are created

#### `syncService.additionalEnvironmentVariables`

* Required: false
* Default: None
* Example:
  ```yaml
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `syncService.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Sync Service deployment

#### `syncService.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

#### `syncService.resources.requests`

* Required: false
* Default:
  ```yaml
  requests:
    memory: "512Mi"
    cpu: "0.5"
  ```
* Description: The resources a node should keep reserved for your pod
*
#### `syncService.resources.limits`

* Required: false
* Default: None
* Description: The maximum resources a pod may consume from a node### Sync Service

### Office Online Integration(OOI)

#### `ooi.enabled`

* Required: false
* Default: `false`
* Description: Enable or disable the Office Online Integration

#### `ooi.replicaCount`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `ooi.image`

* Required: false
* Default: `quay.io/alfresco/alfresco-ooi-service:1.1.2`
* Description: The repository of the docker image that will be used

#### `ooi.imagePullPolicy`

* Required: false
* Default: `IfNotPresent`
* Description: Specify when the pods should pull the image from the repositories

#### `ooi.strategy.type`

* Required: false
* Default: `RollingUpdate`
* Description: Can be set to `Recreate` if you want all your pods to be killed before new ones are created

#### `ooi.additionalEnvironmentVariables`

* Required: false
* Default: None
* Example:
  ```yaml
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `ooi.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Office Online Integration deployment

#### `ooi.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

#### `ooi.resources.requests`

* Required: false
* Default:
  ```yaml
  requests:
    memory: "128Mi"
    cpu: "100m"
  ```
* Description: The resources a node should keep reserved for your pod
*
#### `ooi.resources.limits`

* Required: false
* Default: None
* Description: The maximum resources a pod may consume from a node

### Persistent Storage

### Alfresco

#### `persistentStorage.alfresco.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the creation of a PV and PVC for the ACS pods

#### `persistentStorage.alfresco.storageClassName`

* Required: false
* Default: `scw-bssd`
* Description: Provide what storageClass should be used. For values other then `scw-bssd` `standard` or `efs-storage-class` you will need to make sure that that storage class is created

#### `persistentStorage.alfresco.storage`

* Required: false
* Default: `3`
* Description: The size in GB of the volume that should be reserved

#### `persistentStorage.alfresco.efs.volumeHandle`

* Required: when `persistentStorage.alfresco.storageClassName` is `scw-bssd`
* Default: None
* Description: The volume handle pointing to the AWS EFS location

### Postgres

#### `persistentStorage.postgres.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the creation of a PV and PVC for the PostgresQL pods

#### `persistentStorage.postgres.storageClassName`

* Required: false
* Default: `scw-bssd`
* Description: Provide what storageClass should be used. For values other then `scw-bssd` `standard` or `efs-storage-class` you will need to make sure that that storage class is created

#### `persistentStorage.postgres.storage`

* Required: false
* Default: `2`
* Description: The size in GB of the volume that should be reserved

#### `persistentStorage.postgres.efs.volumeHandle`

* Required: when `persistentStorage.postgres.storageClassName` is `scw-bssd`
* Default: None
* Description: The volume handle pointing to the AWS EFS location

### SOLR

#### `persistentStorage.solr.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the creation of a PV and PVC for the SOLR pods

#### `persistentStorage.solr.storageClassName`

* Required: false
* Default: `scw-bssd`
* Description: Provide what storageClass should be used. For values other then `scw-bssd` `standard` or `efs-storage-class` you will need to make sure that that storage class is created

#### `persistentStorage.solr.storage`

* Required: false
* Default: `3`
* Description: The size in GB of the volume that should be reserved

#### `persistentStorage.solr.efs.volumeHandle`

* Required: when `persistentStorage.solr.storageClassName` is `scw-bssd`
* Default: None
* Description: The volume handle pointing to the AWS EFS location

### Shared File Store

#### `persistentStorage.sharedFileStore.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the creation of a PV and PVC for the Shared File Store pods

#### `persistentStorage.sharedFileStore.storageClassName`

* Required: false
* Default: `scw-bssd`
* Description: Provide what storageClass should be used. For values other then `scw-bssd` `standard` or `efs-storage-class` you will need to make sure that that storage class is created

#### `persistentStorage.sharedFileStore.storage`

* Required: false
* Default: `3`
* Description: The size in GB of the volume that should be reserved

#### `persistentStorage.sharedFileStore.efs.volumeHandle`

* Required: when `persistentStorage.sharedFileStore.storageClassName` is `scw-bssd`
* Default: None
* Description: The volume handle pointing to the AWS EFS location

### Active MQ

#### `persistentStorage.mq.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the creation of a PV and PVC for the Active MQ pods

#### `persistentStorage.mq.storageClassName`

* Required: false
* Default: `scw-bssd`
* Description: Provide what storageClass should be used. For values other then `scw-bssd` `standard` or `efs-storage-class` you will need to make sure that that storage class is created

#### `persistentStorage.mq.storage`

* Required: false
* Default: `3`
* Description: The size in GB of the volume that should be reserved

#### `persistentStorage.mq.efs.volumeHandle`

* Required: when `persistentStorage.mq.storageClassName` is `scw-bssd`
* Default: None
* Description: The volume handle pointing to the AWS EFS location

### Image Credentials

### Private

#### `imageCredentials.private.registry`

* Required: false
* Default: `hub.xenit.eu`
* Description: The registry where the private images are hosted

#### `imageCredentials.private.username`

* Required: true
* Default: None
* Description: The username with which you will pull images from the private repo

#### `imageCredentials.private.password`

* Required: true
* Default: None
* Description: The password for the username with which you will pull images from the private repo

### Alfresco (Quay)

#### `imageCredentials.alfresco.registry`

* Required: false
* Default: `quay.io`
* Description: The registry where alfresco private images are hosted

#### `imageCredentials.alfresco.username`

* Required: true
* Default: None
* Description: The username with which you will pull alfresco images from the alfresco repo

#### `imageCredentials.alfresco.password`

* Required: true
* Default: None
* Description: The password for the username with which you will pull alfresco images from the alfresco repo


