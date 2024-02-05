# Xenit Alfresco Helm Chart

[![Latest version of 'alfresco' @ Cloudsmith](https://api-prd.cloudsmith.io/v1/badges/version/xenit/open-source/helm/alfresco/latest/x/?render=true&show_latest=true)](https://cloudsmith.io/~xenit/repos/open-source/packages/detail/helm/alfresco/latest/)

This is a helm chart for installing Alfresco

## Helm

[![Hosted By: Cloudsmith](https://img.shields.io/badge/OSS%20hosting%20by-cloudsmith-blue?logo=cloudsmith&style=for-the-badge)](https://cloudsmith.com)

Package repository hosting is graciously provided by  [Cloudsmith](https://cloudsmith.com). Cloudsmith is the only fully
hosted, cloud-native, universal package management solution, that enables your organization to create, store and share
packages in any format, to any place, with total confidence.

You can install this helm chart on you K8s cluster. Keep in mind that you will need to add some `--set` statements for
this to work:

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

* To start the cluster you have to create one using kind with the config file as a parameter that is under the directory
  kind:
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

* set up the image pull secrets like in the example and add them to the ```general.imagePullSecrets```
  Example :

```
apiVersion: v1
kind: Secret
metadata:
  name: secretName
  namespace: {{ .Release.Namespace | quote }}
type: kubernetes.io/dockerconfigjson
data:
  .dockerconfigjson: {{- printf "{\"auths\":{\"%s\":{\"username\":\"%s\",\"password\":\"%s\",\"email\":\"%s\",\"auth\":\"%s\"}}}" <<registry>> <<username>> <<password>> (printf "%s:%s" .username .password | b64enc) | b64enc }}
```

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

This helm chart supports a lot of features like share and desktop sync. You are however yourself responsible to provide
an ACS image with the correct amps installed to support these features. Please note that this helm chart is build to
support the xenit open source images. These are build on the official Alfresco Images but have additional K8S support.
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
* Description: You can overwrite here the rollout strategy of deployments. This will be effective on ALL deployments in
  the helm chart that have strategy type RollingUpdate (default)

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
* Description: If you use an image that is not public. then you can create dockerconfigjson secrets on your cluster and
  reference them here. The secrets will be referenced in all Deployments and StatefullSets.

#### `general.serviceType`

* Required: false
* Default: None
* Description: will set a serviceType on the services that are exposed via an ingress. This might be useful for example
  when you are working on AWS infra with an AWS ALB which requires NodePort services

#### `general.db.username`

* Required: false
* Default: None
* Description: Used in the ACS and SyncService pod to access the Database and to set the username of the rootuser of the
  postgres (if enabled)
* Note: If not specified the helm chart will try to reuse the value used in previous deployments. If these are not there
  a random user will be used.

#### `general.db.password`

* Required: false
* Default: None
* Description: Used in the ACS and SyncService pod to access the Database and to set the password of the rootuser of the
  postgres (if enabled)
* Note: If not specified the helm chart will try to reuse the value used in previous deployments. If these are not there
  a random password will be used.

#### `general.networkPolicies.enabled`

* Required: false
* Default: true
* Description: A field to enabled/disable network policies.

#### `general.cni`

* Required: false
* Default: cilium
* Description: A field to tell the helm chart what cni provider your cluster is using. By default we assume cilium. If
  this is not the case you will need to add a network policy to allow the following
* Alfresco to access heartbeat

#### `general.secrets.acs.selfManaged`

* Required: false
* Default: false
* Description: Whether or not you want to provide secrets for the helm chart yourself. This is useful when working on a
  prod environment and you want a secure secret solution (for example Bitnami' Sealed secrets)
* Please note that when you enable this you are yourself responsible to provide a secret acs-secret in the namespace
  that you will install this chart in.
* Secret data expected:

```
  GLOBAL_objectstorage.store.myS3ContentStore.value.accessKey
  GLOBAL_objectstorage.store.myS3ContentStore.value.secretKey
```

#### `general.secrets.mq.selfManaged`

* Required: false
* Default: false
* Description: Whether or not you want to provide secrets for the helm chart yourself. This is useful when working on a
  prod environment and you want a secure secret solution (for example Bitnami' Sealed secrets)
* Please note that when you enable this you are yourself responsible to provide a secret mq-secret in the namespace that
  you will install this chart in.
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
* Description: Whether or not you want to provide secrets for the helm chart yourself. This is useful when working on a
  prod environment and you want a secure secret solution (for example Bitnami' Sealed secrets)
* Please note that when you enable this you are yourself responsible to provide a secret db-secret in the namespace that
  you will install this chart in.
* Secret data expected:

```
  DB_USERNAME
  DB_PASSWORD  
  POSTGRES_USER
  POSTGRES_PASSWORD
```

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

#### `ingress.defaultBackend.service`

* Required: true
* Default: acs-service
* Description: the default service name that ingress will point to

#### `ingress.defaultBackend.port`

* Required: true
* Default: 30000
* Description: the default service port that ingress will point to

#### `ingress.blockAcsSolrApi.enabled`

* Required: false
* Default: `true`
* Description: Enable 403 handler for alfresco api solr endpoints
#### `ingress.blockAcsSolrApi.paths`

* Required: false
* Default: 
```yaml
- /alfresco/s/api/solr
- /alfresco/service/api/solr
- /alfresco/service/api/solr
- /alfresco/wcservice/api/solr
```
* Description: List of paths that are blocked
### ACS

#### `acs.replicas`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `acs.image.registry`

* Required: false
* Default: `docker.io`
* Description: The registry where the docker container can be found in

#### `acs.image.repository`

* Required: false
* Default: `xenit/alfresco-repository-community`
* Description: The repository of the docker image that will be used

#### `acs.image.tag`

* Required: false
* Default: `7.2.0`
* Description: The tag of the docker image that will be used

#### `acs.imagePullPolicy`

* Required: false
* Default: `IfNotPresent`
* Description: Specify when the pods should pull the image from the repositories

#### `acs.livenessProbe`

* Required: false
* Default: 
```
    httpGet:
      path: /alfresco/api/-default-/public/alfresco/versions/1/probes/-live-
      port: 8080
      scheme: HTTP
    failureThreshold: 1
    initialDelaySeconds: 130
    periodSeconds: 20
    successThreshold: 1
    timeoutSeconds: 10
```
* Description: Specify the livenessProbe configuration for acs

#### `acs.readinessProbe`

* Required: false
* Default: 
```
    httpGet:
      path: /alfresco/api/-default-/public/alfresco/versions/1/probes/-ready-
      port: 8080
      scheme: HTTP
    failureThreshold: 6
    initialDelaySeconds: 60
    periodSeconds: 20
    successThreshold: 1
    timeoutSeconds: 10
```
* Description: Specify the readinessProbe configuration for acs

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
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the
  docker container. These will be stored in a config and are hence not safe for sensitive information

#### `acs.envFrom`

* Required: false
* Default: None
* Description: This allows you to add to the acs-container envFrom section. This was added to allow to integrate secrets
  that are not added by this helm chart.
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

#### `acs.serviceAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the acs service

#### `acs.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are
  yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

#### `acs.resources.requests`

* Required: false
* Default:
  ```yaml
  requests:
    memory: "2Gi"
    cpu: "2"
  ```
* Description: The resources a node should keep reserved for your pod

#### `acs.resources.limits`

* Required: false
* Default: None
* Description: The maximum resources a pod may consume from a node

#### `acs.hpa.enabled`

* Required: false
* Default: false
* Description: Whether the ACS deployment should autoscale

#### `acs.hpa.minReplicas`

* Required: false
* Default: 1
* Description: The min ammount of replicas will run when autoscaling

#### `acs.hpa.maxReplicas`

* Required: false
* Default: 10
* Description: The max ammount of replicas will run when autoscaling

#### `acs.hpa.cpu.enabled`

* Required: false
* Default: false
* Description: whether horizontal scaling should trigger on cpu load

#### `acs.hpa.cpu.utilization`

* Required: false
* Default: 70
* Description: The CPU cutover percentage

#### `acs.hpa.memory.enabled`

* Required: false
* Default: true
* Description: whether horizontal scaling should trigger on memory load

#### `acs.hpa.memory.utilization`

* Required: false
* Default: 70
* Description: The Memory cutover percentage

#### `acs.imagePullSecrets`

* Required: false
* Default: None
* Example:
  ```yaml
    - name: privateDockerRepo1Secret
    - name: privateDockerRepo2Secret
  ```
* Description: If you use an image that is not public. then you can create dockerconfigjson secrets on your cluster and
  reference them here.

#### `acs.additionalVolumeMounts`

* Required: false
* Default: None
* Description: A list of configMaps that need to be mounted as volumes to the alfresco pods. Make sure the configMap specified exists. Layout should be as follows:

```yaml
      - mountPath: >-
          /usr/local/tomcat/shared/classes/alfresco/extension/subsystems/Authentication/ldap-ad/oup-ad1	
        name: ldap1-ad-auth-volume	
        readOnly: true	
      - mountPath: >-	
          /usr/local/tomcat/shared/classes/alfresco/extension/subsystems/Authentication/ldap-ad/oup-ad2	
        name: ldap2-ad-auth-volume	
        readOnly: true	
      - mountPath: >-	
          /usr/local/tomcat/shared/classes/alfresco/extension/subsystems/Authentication/ldap-ad/oup-ad3	
        name: ldap3-ad-auth-volume	
        readOnly: true
```

#### `acs.additionalVolumes`

* Required: false
* Default: None
* Description: A list of configMaps that need to be mounted as volumes to the alfresco pods. Make sure the configMap specified exists. Layout should be as follows:

```yaml
      - configMap:
          defaultMode: 420
          items:
            - key: ldap-ad-authentication.properties
              path: ldap-ad-authentication.properties
          name: ldap1-ad-auth-config
        name: ldap1-ad-auth-volume
      - configMap:
          defaultMode: 420
          items:
            - key: ldap-ad-authentication.properties
              path: ldap-ad-authentication.properties
          name: ldap2-ad-auth-config
        name: ldap2-ad-auth-volume
      - configMap:
          defaultMode: 420
          items:
            - key: ldap-ad-authentication.properties
              path: ldap-ad-authentication.properties
          name: ldap3-ad-auth-config
        name: ldap3-ad-auth-volume
```


### Digital Workspace

#### `digitalWorkspace.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the digital workspace

#### `digitalWorkspace.replicas`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `digitalWorkspace.image.registry`

* Required: false
* Default: `quay.io`
* Description: The registry where the docker container can be found in

#### `digitalWorkspace.image.repository`

* Required: false
* Default: `alfresco/alfresco-digital-workspace`
* Description: The repository of the docker image that will be used

#### `digitalWorkspace.image.tag`

* Required: false
* Default: `2.4.2-adw`
* Description: The tag of the docker image that will be used

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
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the
  docker container. These will be stored in a config and are hence not safe for sensitive information

#### `digitalWorkspace.envFrom`

* Required: false
* Default: None
* Description: This allows you to add to the digitalWorkspace-container envFrom section. This was added to allow to
  integrate secrets
  that are not added by this helm chart.
* Example:

```yaml
  - secretRef:
    name: s3-secret
```

#### `digitalWorkspace.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Digital Workspace deployment

#### `digitalWorkspace.serviceAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Digital Workspace service

#### `digitalWorkspace.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are
  yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

#### `digitalWorkspace.resources.requests`

* Required: false
* Default:
  ```yaml
  requests:
    memory: "256Mi"
    cpu: "150m"
  ```
* Description: The resources a node should keep reserved for your pod

#### `digitalWorkspace.resources.limits`

* Required: false
* Default: None
* Description: The maximum resources a pod may consume from a node

#### `digitalWorkspace.imagePullSecrets`

* Required: false
* Default: None
* Example:
  ```yaml
    - name: privateDockerRepo1Secret
    - name: privateDockerRepo2Secret
  ```
* Description: If you use an image that is not public. then you can create dockerconfigjson secrets on your cluster and
  reference them here.

### Share

#### `share.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the share

#### `share.mergeAcsShare`

* Required: false
* Default: false
* Description: If set to `true` the Share container will be installed inside the ACS pod.

#### `share.replicas`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `share.image.registry`

* Required: false
* Default: `docker.io`
* Description: The registry where the docker container can be found in

#### `share.image.repository`

* Required: false
* Default: `alfresco-share-community`
* Description: The repository of the docker image that will be used

#### `share.image.tag`

* Required: false
* Default: `7.2.0`
* Description: The tag of the docker image that will be used

#### `share.imagePullPolicy`

* Required: false
* Default: `IfNotPresent`
* Description: Specify when the pods should pull the image from the repositories

#### `share.livenessProbe.failureThreshold`

* Required: false
* Default: 1
* Description: Specify the livenessProbe failure thresh hold fp how many consecutive failure before it stops probing

#### `share.livenessProbe.initialDelaySeconds`

* Required: false
* Default: 130
* Description: Specify the livenessProbe initial delay before it starts probing

#### `share.livenessProbe.periodSeconds`

* Required: false
* Default: 20
* Description: Specify the livenessProbe period between probes

#### `share.livenessProbe.successThreshold`

* Required: false
* Default: 1
* Description: Specify the livenessProbe success thresh hold for how many consecutive successes for the probe to be
  considered successful after having failed

#### `share.livenessProbe.timeoutSeconds`

* Required: false
* Default: 10
* Description: Specify the livenessProbe timeout for probes to be considered as failure

#### `share.readinessProbe.failureThreshold`

* Required: false
* Default: 6
* Description: Specify the readinessProbe failure thresh hold fp how many consecutive failure before it stops probing

#### `share.readinessProbe.initialDelaySeconds`

* Required: false
* Default: 60
* Description: Specify the readinessProbe initial delay before it starts probing

#### `share.readinessProbe.periodSeconds`

* Required: false
* Default: 20
* Description: Specify the readinessProbe period between probes

#### `share.readinessProbe.successThreshold`

* Required: false
* Default: 1
* Description: Specify the readinessProbe success thresh hold for how many consecutive successes for the probe to be
  considered successful after having failed

#### `share.readinessProbe.timeoutSeconds`

* Required: false
* Default: 10
* Description: Specify the readinessProbe timeout for probes to be considered as failure

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
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the
  docker container. These will be stored in a config and are hence not safe for sensitive information

#### `share.envFrom`

* Required: false
* Default: None
* Description: This allows you to add to the share-container envFrom section. This was added to allow to integrate
  secrets
  that are not added by this helm chart.
* Example:

```yaml
  - secretRef:
    name: s3-secret
```

#### `share.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Share deployment

#### `share.serviceAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Share service

#### `share.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are
  yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

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

#### `share.imagePullSecrets`

* Required: false
* Default: None
* Example:
  ```yaml
    - name: privateDockerRepo1Secret
    - name: privateDockerRepo2Secret
  ```
* Description: If you use an image that is not public. then you can create dockerconfigjson secrets on your cluster and
  reference them here.

### Active MQ

#### `mq.adminLogin`

* Required: false
* Default: None
* Description: Sets the username of the admin user of the MQ
* Note: If not specified the helm chart will try to reuse the value used in previous deployments. If these are not there
  a random login will be used.

#### `mq.adminPassword`

* Required: false
* Default: None
* Description: Sets the password of the admin user of the MQ
* Note: If not specified the helm chart will try to reuse the value used in previous deployments. If these are not there
  a random password will be used.

#### `mq.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the Active MQ

#### `mq.replicas`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `mq.image.registry`

* Required: false
* Default: `docker.io`
* Description: The registry where the docker container can be found in

#### `mq.image.repository`

* Required: false
* Default: `alfresco/alfresco-activemq`
* Description: The repository of the docker image that will be used

#### `mq.image.tag`

* Required: false
* Default: `5.16.1`
* Description: The tag of the docker image that will be used

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
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the
  docker container. These will be stored in a config and are hence not safe for sensitive information

#### `mq.envFrom`

* Required: false
* Default: None
* Description: This allows you to add to the mq-container envFrom section. This was added to allow to integrate secrets
  that are not added by this helm chart.
* Example:

```yaml
  - secretRef:
    name: s3-secret
```

#### `mq.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Active MQ deployment

#### `mq.serviceAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Active MQ service

#### `mq.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are
  yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

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

#### `mq.imagePullSecrets`

* Required: false
* Default: None
* Example:
  ```yaml
    - name: privateDockerRepo1Secret
    - name: privateDockerRepo2Secret
  ```
* Description: If you use an image that is not public. then you can create dockerconfigjson secrets on your cluster and
  reference them here.

### Postgresql

#### `postgresql.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the PostgresQl

#### `postgresql.replicas`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `postgresql.image.registry`

* Required: false
* Default: `docker.io`
* Description: The registry where the docker container can be found in

#### `postgresql.image.repository`

* Required: false
* Default: `xenit/postgres`
* Description: The repository of the docker image that will be used

#### `postgresql.image.tag`

* Required: false
* Default: `latest`
* Description: The tag of the docker image that will be used

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
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the
  docker container. These will be stored in a config and are hence not safe for sensitive information

#### `postgresql.envFrom`

* Required: false
* Default: None
* Description: This allows you to add to the postgresql-container envFrom section. This was added to allow to integrate
  secrets
  that are not added by this helm chart.
* Example:

```yaml
  - secretRef:
    name: s3-secret
```

#### `postgresql.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the PostgresQl deployment

#### `postgresql.serviceAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the PostgresQl service

#### `postgresql.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are
  yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

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

#### `postgresql.imagePullSecrets`

* Required: false
* Default: None
* Example:
  ```yaml
    - name: privateDockerRepo1Secret
    - name: privateDockerRepo2Secret
  ```
* Description: If you use an image that is not public. then you can create dockerconfigjson secrets on your cluster and
  reference them here.

### SOLR

#### `solr.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the Solr

#### `solr.replicas`

* Required: false
* Default: `2`
* Description: The number of pods that will be running

* #### `solr.podManagementPolicy`

* Required: false
* Default: `Parallel`
* Description: The way to manage multiple pod launching or termination possible values are `Parallel` or `OrderedReady`find more information in [Docs](https://kubernetes.io/docs/concepts/workloads/controllers/statefulset/#parallel-pod-management)

#### `solr.image.registry`

* Required: false
* Default: `docker.io`
* Description: The registry where the docker container can be found in

#### `solr.image.repository`

* Required: false
* Default: `xenit/alfresco-solr6-xenit`
* Description: The repository of the docker image that will be used

#### `solr.image.tag`

* Required: false
* Default: `2.0.6`
* Description: The tag of the docker image that will be used

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
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the
  docker container. These will be stored in a config and are hence not safe for sensitive information

#### `solr.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Solr deployment

#### `solr.serviceAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Solr service

#### `solr.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are
  yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

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

#### `solr.imagePullSecrets`

* Required: false
* Default: None
* Example:
  ```yaml
    - name: privateDockerRepo1Secret
    - name: privateDockerRepo2Secret
  ```
* Description: If you use an image that is not public. then you can create dockerconfigjson secrets on your cluster and
  reference them here.

#### `solr.autoBackup.enabled`

* Required: false
* Default: false
* Description:
    - Enable or disable the Solr auto backup job , it will create a cron job that calls solr
      to start a backup snapshot.
    - backup repository environment variables needs to be set if enabled:
   ```yaml
    - JAVA_OPTS_S3_ENDPOINT=-DS3_ENDPOINT=<endpoint_s3_protocol>
    - JAVA_OPTS_S3_REGION=-DS3_REGION=<bucket_s3_region>
    - JAVA_OPTS_S3_BUCKET_NAME=-DS3_BUCKET_NAME=<bucket_name>
    - JAVA_OPTS_AWS_ACCESS_KEY_ID=-Daws.accessKeyId=<access_key>
    - JAVA_OPTS_AWS_SECRET_ACCESS_KEY=-Daws.secretKey=<secret_key>
  ```

#### `solr.autoBackup.cron`

* Required: false
* Default: 0 * * * *
* Description: if `solr.autoBackup.enabled` is true then a cron job will be created with this value as its cron

#### `solr.autoBackup.backupUrl`

* Required: false
*

Default: http://solr-service:30300/solr/alfresco/replication?command=backup&repository=s3&location=s3:///&numberToKeep=3

* Description: if `solr.autoBackup.enabled` is true then a cron job will be created that will curl this url

#### `solr.readinessProbe.enabled`

* Required: false
* Default: true
* Description: Enable or disable the job readiness probe

#### `solr.readinessProbe.failureThreshold`

* Required: false
* Default: 3
* Description: Specify the readinessProbe failure thresh hold fp how many consecutive failure before it stops probing

#### `solr.readinessProbe.initialDelaySeconds`

* Required: false
* Default: 30
* Description: Specify the readinessProbe initial delay before it starts probing

#### `solr.readinessProbe.periodSeconds`

* Required: false
* Default: 10
* Description: Specify the readinessProbe period between probes

#### `solr.readinessProbe.successThreshold`

* Required: false
* Default: 1
* Description: Specify the readinessProbe success thresh hold for how many consecutive successes for the probe to be
  considered successful after having failed

#### `solr.readinessProbe.timeoutSeconds`

* Required: false
* Default: 10
* Description: Specify the readinessProbe timeout for probes to be considered as failure

### Transform Services

#### `transformServices.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the Transform Services

#### `transformServices.imagePullSecrets`

* Required: false
* Default: None
* Example:
  ```yaml
    - name: privateDockerRepo1Secret
    - name: privateDockerRepo2Secret
  ```
* Description: If you use an image that is not public. then you can create dockerconfigjson secrets on your cluster and
  reference them here. they will be referenced in all transform services Deployments.

### Shared File Store

#### `transformServices.sharedFileStore.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the Shared File Store

#### `transformServices.sharedFileStore.replicas`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `transformServices.sharedFileStore.image.registry`

* Required: false
* Default: `docker.io`
* Description: The registry where the docker container can be found in

#### `transformServices.sharedFileStore.image.repository`

* Required: false
* Default: `alfresco/alfresco-shared-file-store`
* Description: The repository of the docker image that will be used

#### `transformServices.sharedFileStore.image.tag`

* Required: false
* Default: `0.16.1`
* Description: The tag of the docker image that will be used

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
  scheduler.cleanup.interval: "10800000"
  scheduler.content.age.millis: "43200000"
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the
  docker container. These will be stored in a config and are hence not safe for sensitive information

#### `transformServices.sharedFileStore.envFrom`

* Required: false
* Default: None
* Description: This allows you to add to the sharedFileStore-container envFrom section. This was added to allow to
  integrate secrets
  that are not added by this helm chart.
* Example:

```yaml
  - secretRef:
    name: s3-secret
```

#### `transformServices.sharedFileStore.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Transform Services deployment

#### `transformServices.sharedFileStore.serviceAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Transform Services service

#### `transformServices.sharedFileStore.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are
  yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

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

#### `transformServices.sharedFileStore.imagePullSecrets`

* Required: false
* Default: None
* Example:
  ```yaml
    - name: privateDockerRepo1Secret
    - name: privateDockerRepo2Secret
  ```
* Description: If you use an image that is not public. then you can create dockerconfigjson secrets on your cluster and
  reference them here.

#### `transformServices.sharedFileStore.initVolumes`

* Required: false
* Default: `true`
* Description: Enable or disable the setting of /tmp/Alfresco owner to sfs user

### Transform Core All In One

#### `transformServices.transformCoreAio.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the Transform Core All In One

#### `transformServices.transformCoreAio.replicas`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `transformServices.transformCoreAio.image.registry`

* Required: false
* Default: `docker.io`
* Description: The registry where the docker container can be found in

#### `transformServices.transformCoreAio.image.repository`

* Required: false
* Default: `alfresco/alfresco-transform-core-aio`
* Description: The repository of the docker image that will be used

#### `transformServices.transformCoreAio.image.tag`

* Required: false
* Default: `latest`
* Description: The tag of the docker image that will be used

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
    livenessPercent: "150"
    livenessTransformPeriodSeconds: "600"
    maxTransforms: "100000"
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the
  docker container. These will be stored in a config and are hence not safe for sensitive information

#### `transformServices.transformCoreAio.envFrom`

* Required: false
* Default: None
* Description: This allows you to add to the transformCoreAio-container envFrom section. This was added to allow to
  integrate secrets
  that are not added by this helm chart.
* Example:

```yaml
  - secretRef:
    name: s3-secret
```
#### `transformServices.transformCoreAio.livenessProbe.enabled`

* Required: false
* Default: true
* Description: will enable liveness and readiness probes  
additional settings can be added through additionalEnvironmentVariables.  
```yaml
  livenessPercent: "150"
  livenessTransformPeriodSeconds: "600"
  maxTransforms: "100000"
  ```


#### `transformServices.transformCoreAio.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
     prometheus.io/path: actuator/prometheus
     prometheus.io/scrape: 'true'
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Transform Core All In One

#### `transformServices.transformCoreAio.serviceAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Transform Core All In One
  service

#### `transformServices.transformCoreAio.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are
  yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

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

#### `transformServices.transformCoreAio.imagePullSecrets`

* Required: false
* Default: None
* Example:
  ```yaml
    - name: privateDockerRepo1Secret
    - name: privateDockerRepo2Secret
  ```
* Description: If you use an image that is not public. then you can create dockerconfigjson secrets on your cluster and
  reference them here.

### Transform Router

#### `transformServices.transformRouter.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the Transform Router

#### `transformServices.transformRouter.replicas`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `transformServices.transformRouter.image.registry`

* Required: false
* Default: `quay.io`
* Description: The registry where the docker container can be found in

#### `transformServices.transformRouter.image.repository`

* Required: false
* Default: `alfresco/alfresco-transform-router`
* Description: The repository of the docker image that will be used

#### `transformServices.transformRouter.image.tag`

* Required: false
* Default: `1.5.2`
* Description: The tag of the docker image that will be used

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
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the
  docker container. These will be stored in a config and are hence not safe for sensitive information

#### `transformServices.transformRouter.envFrom`

* Required: false
* Default: None
* Description: This allows you to add to the transformRouter-container envFrom section. This was added to allow to
  integrate secrets
  that are not added by this helm chart.
* Example:

```yaml
  - secretRef:
    name: s3-secret
```

#### `transformServices.transformRouter.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Transform Router deployment

#### `transformServices.transformRouter.serviceAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Transform Router service

#### `transformServices.transformRouter.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are
  yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

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

#### `transformServices.transformRouter.imagePullSecrets`

* Required: false
* Default: None
* Example:
  ```yaml
    - name: privateDockerRepo1Secret
    - name: privateDockerRepo2Secret
  ```
* Description: If you use an image that is not public. then you can create dockerconfigjson secrets on your cluster and
  reference them here.

### Sync Service

#### `syncService.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the Sync Service

#### `syncService.replicas`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `syncService.image.registry`

* Required: false
* Default: `quay.io`
* Description: The registry where the docker container can be found in

#### `syncService.image.repository`

* Required: false
* Default: `alfresco/service-sync`
* Description: The repository of the docker image that will be used

#### `syncService.image.tag`

* Required: false
* Default: `3.4.0`
* Description: The tag of the docker image that will be used

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
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the
  docker container. These will be stored in a config and are hence not safe for sensitive information

#### `syncService.envFrom`

* Required: false
* Default: None
* Description: This allows you to add to the syncService-container envFrom section. This was added to allow to integrate
  secrets
  that are not added by this helm chart.
* Example:

```yaml
  - secretRef:
    name: s3-secret
```

#### `syncService.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Sync Service deployment

#### `syncService.serviceAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Sync Service service

#### `syncService.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are
  yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

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

#### `syncService.imagePullSecrets`

* Required: false
* Default: None
* Example:
  ```yaml
    - name: privateDockerRepo1Secret
    - name: privateDockerRepo2Secret
  ```
* Description: If you use an image that is not public. then you can create dockerconfigjson secrets on your cluster and
  reference them here.

### Office Online Integration(OOI)

#### `ooi.enabled`

* Required: false
* Default: `false`
* Description: Enable or disable the Office Online Integration

#### `ooi.replicas`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `ooi.image.registry`

* Required: false
* Default: `quay.io`
* Description: The registry where the docker container can be found in

#### `ooi.image.repository`

* Required: false
* Default: `alfresco/alfresco-ooi-service`
* Description: The repository of the docker image that will be used

#### `ooi.image.tag`

* Required: false
* Default: `1.1.2`
* Description: The tag of the docker image that will be used

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
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the
  docker container. These will be stored in a config and are hence not safe for sensitive information

#### `ooi.envFrom`

* Required: false
* Default: None
* Description: This allows you to add to the ooi-container envFrom section. This was added to allow to integrate secrets
  that are not added by this helm chart.
* Example:

```yaml
  - secretRef:
    name: s3-secret
```

#### `ooi.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Office Online Integration
  deployment

#### `ooi.serviceAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the Office Online Integration
  service

#### `ooi.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are
  yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

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

#### `ooi.imagePullSecrets`

* Required: false
* Default: None
* Example:
  ```yaml
    - name: privateDockerRepo1Secret
    - name: privateDockerRepo2Secret
  ```
* Description: If you use an image that is not public. then you can create dockerconfigjson secrets on your cluster and
  reference them here.

### Persistent Storage

### Alfresco

#### `persistentStorage.alfresco.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the creation of a PV and PVC for the ACS pods

#### `persistentStorage.alfresco.storageClassName`

* Required: false
* Default: `scw-bssd`
* Description: Provide what storageClass should be used. For values other then `scw-bssd` `standard`
  or `efs-storage-class` you will need to make sure that that storage class is created

#### `persistentStorage.alfresco.storage`

* Required: false
* Default: `3`
* Description: The size in GB of the volume that should be reserved

#### `persistentStorage.alfresco.efs.volumeHandle`

* Required: when `persistentStorage.alfresco.storageClassName` is `scw-bssd`
* Default: None
* Description: The volume handle pointing to the AWS EFS location

#### `persistentStorage.alfresco.additionalClaims`

* Required: false
* Default: None
* Description: A list of additional volume claims that can be added to the alfresco pods. Layout should be as follows:

```yaml
      - name: name1
        mountPath: /apps/example
        subPath: subPath/example
        storageClassName: "standard"
        storage: 2
        efs:
          volumeHandle: "efs-identifier"
```

### Postgres

#### `persistentStorage.postgres.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the creation of a PV and PVC for the PostgresQL pods

#### `persistentStorage.postgres.storageClassName`

* Required: false
* Default: `scw-bssd`
* Description: Provide what storageClass should be used. For values other then `scw-bssd` `standard`
  or `efs-storage-class` you will need to make sure that that storage class is created

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
* Description: Provide what storageClass should be used. For values other then `scw-bssd` `standard`
  or `efs-storage-class` you will need to make sure that that storage class is created

#### `persistentStorage.solr.storage`

* Required: false
* Default: `3`
* Description: The size in GB of the volume that should be reserved

#### `persistentStorage.solr.efs.volumeHandle`

* Required: when `persistentStorage.solr.storageClassName` is `scw-bssd`
* Default: None
* Description: The volume handle pointing to the AWS EFS location

### SOLR BACKUP

#### `persistentStorage.solrBackup.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the creation of a PV and PVC for the SOLR BACKUP for SOLR pods

#### `persistentStorage.solrBackup.storageClassName`

* Required: false
* Default: `scw-bssd`
* Description: Provide what storageClass should be used. For values other then `scw-bssd` `standard`
  or `efs-storage-class` you will need to make sure that that storage class is created

#### `persistentStorage.solrBackup.storage`

* Required: false
* Default: `3`
* Description: The size in GB of the volume that should be reserved

#### `persistentStorage.solrBackup.efs.volumeHandle`

* Required: when `persistentStorage.solrBackup.storageClassName` is `scw-bssd`
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
* Description: Provide what storageClass should be used. For values other then `scw-bssd` `standard`
  or `efs-storage-class` you will need to make sure that that storage class is created

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


#### `persistentStorage.mq.initVolumes`

* Required: false
* Default: `true`
* Description: Enable or disable the setting of mq data owner to amq user

#### `persistentStorage.mq.storageClassName`

* Required: false
* Default: `scw-bssd`
* Description: Provide what storageClass should be used. For values other then `scw-bssd` `standard`
  or `efs-storage-class` you will need to make sure that that storage class is created

#### `persistentStorage.mq.storage`

* Required: false
* Default: `3`
* Description: The size in GB of the volume that should be reserved

#### `persistentStorage.mq.efs.volumeHandle`

* Required: when `persistentStorage.mq.storageClassName` is `scw-bssd`
* Default: None
* Description: The volume handle pointing to the AWS EFS location
