# Xenit Alfresco Helm Chart

## Dev Requirements
Make sure you have the following installed:
* Kubectl: https://kubernetes.io/docs/tasks/tools/#kubectl
* Helm: https://helm.sh/docs/intro/install/

## Cluster Requirements
Make sure that the cluster you are installing Alfresco on has the following
* An ingress controller installed

## Cluster Requirements
This helm chart supports a lot of features like share and desktop sync. You are however yourself responsible to provide an ACS image with the correct amps installed to support these features

## Configuration

### Global

#### `global.strategy`

* Required: false
* Default: 
  ```
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 1
    maxUnavailable: 0
  ```
* Description: You can overwrite here the rollout strategy of deployments. This will be effective on ALL deployments in the helm chart that have strategy type RollingUpdate (default)

#### `global.podAnnotations`

* Required: false
* Default: None
* Example: 
  ```
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to ALL deployments and statefullSets

#### `global.imagePullSecrets`

* Required: false
* Default: None
* Example: 
  ```
    - name: privateDockerRepo1Secret
    - name: privateDockerRepo2Secret
  ```
* Description: If you use an image that is hosted on a private repo besides the xenit repo or the quay alfresco repo you can create secrets on your cluster and reference them here. The secrets will be referenced in all Deployments and StatefullSets.

#### `global.serviceType`

* Required: false
* Default: None
* Description: will set a serviceType on the services that are exposed via an ingress. This might be useful for example when you are working on AWS infra with an AWS ALB which requires NodePort services

#### `global.dbUser`

* Required: true
* Default: None
* Description: Used in the ACS and SyncService pod to access the Database and to set the username of the rootuser of the postgres (if enabled)

#### `global.dbPassword`

* Required: true
* Default: None
* Description: Used in the ACS and SyncService pod to access the Database and to set the password of the rootuser of the postgres (if enabled)

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

### ACS

#### `acs.replicaCount`

* Required: false
* Default: `2`
* Description: The number of pods that will be running

#### `acs.image.repository`

* Required: true
* Default: None
* Description: The repository of the docker image that will be used


#### `acs.image.tag`

* Required: false
* Default: None
* Description: The tag of the docker image that will be used

#### `acs.image.pullPolicy`

* Required: false
* Default: `IfNotPresent`
* Description: Specify when the pods should pull the image from the repositories

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

#### `acs.s3Datastore.enabled`

* Required: false
* Default: `false`
* Description: Set to true if working with s3

#### `acs.s3Datastore.bucketName`

* Required: when `acs.s3Datastore.enabled` is `true`
* Default: None
* Description: Set the bucketName of the bucket you want to use

#### `acs.s3Datastore.bucketLocation`

* Required: when `acs.s3Datastore.enabled` is `true`
* Default: None
* Description: Set the bucketLocation of the bucket you want to use

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
  ```
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `acs.podAnnotations`

* Required: false
* Default: None
* Example:
  ```
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the acs deployment

#### `acs.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

### Digital Workspace

#### `digitalWorkspace.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the digital workspace

#### `digitalWorkspace.replicaCount`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `digitalWorkspace.image.repository`

* Required: true
* Default: `quay.io/alfresco/alfresco-digital-workspace`
* Description: The repository of the docker image that will be used


#### `digitalWorkspace.image.tag`

* Required: false
* Default: `2.4.2-adw`
* Description: The tag of the docker image that will be used

#### `digitalWorkspace.image.pullPolicy`

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
  ```
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `digitalWorkspace.podAnnotations`

* Required: false
* Default: None
* Example:
  ```
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Digital Workspace deployment

#### `digitalWorkspace.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

### Share

#### `share.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the share

#### `share.replicaCount`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `share.image.repository`

* Required: true
* Default: `xenit/alfresco-share-community`
* Description: The repository of the docker image that will be used


#### `share.image.tag`

* Required: false
* Default: `7.0`
* Description: The tag of the docker image that will be used

#### `share.image.pullPolicy`

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
  ```
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `share.podAnnotations`

* Required: false
* Default: None
* Example:
  ```
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Share deployment

#### `share.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

### Active MQ

#### `mq.adminlogin`

* Required: when `mq.enabled` is `true`
* Default: None
* Description: Sets the username of the admin user of the MQ

#### `mq.adminPassword`

* Required: when `mq.enabled` is `true`
* Default: None
* Description: Sets the password of the admin user of the MQ

#### `mq.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the Active MQ

#### `mq.replicaCount`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `mq.image.repository`

* Required: true
* Default: `alfresco/alfresco-activemq`
* Description: The repository of the docker image that will be used


#### `mq.image.tag`

* Required: false
* Default: `5.16.1`
* Description: The tag of the docker image that will be used

#### `mq.image.pullPolicy`

* Required: false
* Default: `IfNotPresent`
* Description: Specify when the pods should pull the image from the repositories

#### `mq.strategy.type`

* Required: false
* Default: `RollingUpdate`
* Description: Can be set to `Recreate` if you want all your pods to be killed before new ones are created

#### `mq.additionalEnvironmentVariables`

* Required: false
* Default: None
* Example:
  ```
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `mq.podAnnotations`

* Required: false
* Default: None
* Example:
  ```
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Active MQ deployment

#### `mq.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

### PostgresQl

#### `postgresql.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the PostgresQl

#### `postgresql.replicaCount`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `postgresql.image.repository`

* Required: true
* Default: `docker.io/xenit/postgres`
* Description: The repository of the docker image that will be used


#### `postgresql.image.tag`

* Required: false
* Default: `9.6.23`
* Description: The tag of the docker image that will be used

#### `postgresql.image.pullPolicy`

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
  ```
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `postgresql.podAnnotations`

* Required: false
* Default: None
* Example:
  ```
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the PostgresQl deployment

#### `postgresql.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

### SOLR

#### `solr.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the Solr

#### `solr.replicaCount`

* Required: false
* Default: `2`
* Description: The number of pods that will be running

#### `solr.image.repository`

* Required: true
* Default: `hub.xenit.eu/alfresco-enterprise/alfresco-solr6`
* Description: The repository of the docker image that will be used


#### `solr.image.tag`

* Required: false
* Default: `2.0.2`
* Description: The tag of the docker image that will be used

#### `solr.image.pullPolicy`

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
  ```
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `solr.podAnnotations`

* Required: false
* Default: None
* Example:
  ```
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Solr deployment

#### `solr.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

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

#### `transformServices.sharedFileStore.image.repository`

* Required: true
* Default: `quay.io/alfresco/alfresco-shared-file-store`
* Description: The repository of the docker image that will be used


#### `transformServices.sharedFileStore.image.tag`

* Required: false
* Default: `0.16.0`
* Description: The tag of the docker image that will be used

#### `transformServices.sharedFileStore.image.pullPolicy`

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
  ```
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `transformServices.sharedFileStore.podAnnotations`

* Required: false
* Default: None
* Example:
  ```
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Transform Services deployment

#### `transformServices.sharedFileStore.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

### Transform Core All In One

#### `transformServices.transformCoreAio.replicaCount`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `transformServices.transformCoreAio.image.repository`

* Required: true
* Default: `alfresco/alfresco-transform-core-aio`
* Description: The repository of the docker image that will be used


#### `transformServices.transformCoreAio.image.tag`

* Required: false
* Default: None
* Description: The tag of the docker image that will be used

#### `transformServices.transformCoreAio.image.pullPolicy`

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
  ```
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `transformServices.transformCoreAio.podAnnotations`

* Required: false
* Default: None
* Example:
  ```
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Transform Core All In One deployment

#### `transformServices.transformCoreAio.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

### Transform Router

#### `transformServices.transformRouter.replicaCount`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `transformServices.transformRouter.image.repository`

* Required: true
* Default: `quay.io/alfresco/alfresco-transform-router`
* Description: The repository of the docker image that will be used


#### `transformServices.transformRouter.image.tag`

* Required: false
* Default: `1.5.2`
* Description: The tag of the docker image that will be used

#### `transformServices.transformRouter.image.pullPolicy`

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
  ```
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `transformServices.transformRouter.podAnnotations`

* Required: false
* Default: None
* Example:
  ```
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Transform Router deployment

#### `transformServices.transformRouter.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

### Sync Service

#### `syncService.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the Sync Service

#### `syncService.replicaCount`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `syncService.image.repository`

* Required: true
* Default: `docker.io/xenit/postgres`
* Description: The repository of the docker image that will be used


#### `syncService.image.tag`

* Required: false
* Default: `9.6.23`
* Description: The tag of the docker image that will be used

#### `syncService.image.pullPolicy`

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
  ```
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the docker container. These will be stored in a config and are hence not safe for sensitive information

#### `syncService.podAnnotations`

* Required: false
* Default: None
* Example:
  ```
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Sync Service deployment

#### `syncService.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

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


