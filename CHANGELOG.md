# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), with additional info about the
chronology things are added/fixed/changed and - where possible - links to the PRs involved.

### Changes
[v0.7.2]
* added acs.customLivenessProbe

[v0.7.1]
* add flag to enable/disable enterprise-only functionality
* remove HorizontalPodAutoscaler as it was not functional
* set postgresql default strategy to Recreate

[v0.7.0]
* rename blocked solr path to blocked paths
* split default backend and default path

[v0.6.9]
* solve inconsistency with mq.serviceAnnotations

[v0.6.8]
* Add `ingress.defaultBackend.mapToRootOnly` to make defaultBackend creation optional.
* Add /ngnxhealth to nginx-default

[XENOPS-1166]
* add init containers to acs deployment
* fix alfresco additionalClaims split chart

[XENOPS-1164]  date: 10 May 2024
* remove defaultBackend from ingress rules, this should not be set by individual namespace resources
* defaultBackend will point to new nginx-default-service providing 404 if page not found.
* defaultBackend will be mapped to default ingress root path for the alfresco host only



[XENOPS-1161] 
* change liveness probe threshold to trigger after readiness probe failure to avoid looping restarts on slow systems 
 

* make acs liveliness and readiness completely overridable
* make transformation services components each enable configurable
* remove mq config in other deployments when mq is disabled

[EUIAUDIT23-7] 
* correct cilium ooi connections to sharepoint online

* fix the location of service account in solr and remove volume mounts when pvc is disabled for solr
* make init container for mq and sfs configurable for file permission creation

[XENOPS-1123]

* remove share-file-store cleanup interval from configmap
* fix liveness probe share and transform-core-aio

[XM2C-158]

* Share probe the same as acs probe when they are in the same pod
* Update share readiness check

[XM2C-156]

* make podManagementPolicy editable in solr statefulset

[XM2C-155]

* Remove service type from Solr service


* remove init containers of the shared file store container


* Disable cpu autoscaling by default
* Option to merge Share and ACS into 1 pod (sticky session workaround)

[XM2C-127]

* make default backend in ingress in helm alfresco settable by values


* Add HorizontalPodAutoscaler for ACS
* Add -pvc to the additionalClaim in the deployment
* Enable solr readiness probe by default
* Add support for envFrom values or all deployments

[XM2C-91]

* Split SOLR Backup from normal mount

[XM2C-89]

* make repository and registry and tag the only way to set up image
* add alfresco-port to solr-config
* make solr readiness probe disabled by default
* fix transform-router-to-shared-file-store network policy

[XM2C-90]

* add support for custom alfresco mounts and split image values to registry , repository tag for renovate bot
* add renovate

[XM2C-68]

* remove default pull secrets

[XM2C-55]

* Rename all `replicaCount` properties to `replicas`

[XM2C-54]

* Rename all `image.repository` properties to `image`
* Rename all `image.pullPolicy` properties to `imagePullPolicy`

[XM2C-30]

* added checksum of configmap and secrets for deployment for **Automatically Roll Deployments**
* changed mq default strategy from RollingUpdate to Recreate because of conflict in updating 

