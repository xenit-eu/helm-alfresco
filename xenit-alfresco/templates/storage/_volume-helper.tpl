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
    - ReadWriteOnce
  resources:
    requests:
      storage: {{ $storage }}Gi
  storageClassName: {{ $storageClassName }}
---
{{- if eq $storageClassName "standard" -}}
apiVersion: v1
kind: PersistentVolume
metadata:
  name: {{ $name }}-pv
spec:
  capacity:
    storage: {{ $storage }}Gi
  volumeMode: Filesystem
  accessModes:
    - ReadWriteMany
  persistentVolumeReclaimPolicy: Retain
  storageClassName: {{ $storageClassName }}
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
{{- if eq $storageClassName "efs-storage-class" -}}
apiVersion: v1
kind: PersistentVolume
metadata:
  name: {{ $name }}-pv
spec:
  capacity:
    storage: {{ $storage }}Gi
  volumeMode: Filesystem
  accessModes:
    - ReadWriteMany
  persistentVolumeReclaimPolicy: Retain
  storageClassName: {{ $storageClassName }}

  csi:
    driver: efs.csi.aws.com
    volumeHandle: {{ $efsVolumeHandle }}
{{- end }}
{{- end }}