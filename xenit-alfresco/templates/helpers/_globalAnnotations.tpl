{{- define "globalPodAnnotations" }}
{{- if .Values.general.podAnnotations }}
{{ toYaml .Values.general.podAnnotations }}
{{- end }}
{{- end }}