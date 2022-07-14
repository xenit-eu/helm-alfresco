{{- define "hepers.volumeHelper" }}
{{- $namespace := index . 0 }}
{{- $name := index . 1 }}
{{- $storageClassName := index . 2 }}
{{- $storage := index . 3 }}
{{- $efsVolumeHandle := index . 4 }}
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: {{ $name }}-pvc
  namespace: {{ $namespace | quote }}
spec:
  accessModes:
    {{- if eq $storageClassName "efs-storage-class" }}
    - ReadWriteMany
    {{- else }}
    - ReadWriteOnce
    {{- end }}
  resources:
    requests:
      storage: {{ $storage }}Gi
  {{- if ne $storageClassName "" }}
  storageClassName: {{ $storageClassName }}
  {{- end }}
---
{{- if eq $storageClassName "standard" }}
apiVersion: v1
kind: PersistentVolume
metadata:
  name: {{ $name }}-pv
spec:
  capacity:
    storage: {{ $storage }}Gi
  volumeMode: Filesystem
  accessModes:
    - ReadWriteOnce
  persistentVolumeReclaimPolicy: Retain
  {{- if ne $storageClassName "" }}
  storageClassName: {{ $storageClassName }}
  {{- end }}
  claimRef:
    apiVersion: v1
    kind: PersistentVolumeClaim
    name: {{ $name }}-pvc
    namespace: {{ $namespace }}
  hostPath:
    path: /var/local-path-provisioner/shared-storage/alfresco-volume-claim
    type: DirectoryOrCreate
{{- end }}
---
{{- if eq $storageClassName "efs-storage-class" }}
apiVersion: v1
kind: PersistentVolume
metadata:
  name: {{ $name }}-pv
spec:
  capacity:
    storage: {{ $storage }}Gi
  volumeMode: Filesystem
  claimRef:
    apiVersion: v1
    kind: PersistentVolumeClaim
    name: {{ $name }}-pvc
    namespace: {{ $namespace }}
  accessModes:
    - ReadWriteMany
  persistentVolumeReclaimPolicy: Retain
  {{- if ne $storageClassName "" }}
  storageClassName: {{ $storageClassName }}
  {{- end }}
  csi:
    driver: efs.csi.aws.com
    volumeHandle: {{ $efsVolumeHandle }}
{{- end }}
{{- end }}