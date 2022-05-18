{{- define "globalAnnotations" }}
{{- if .Values.global.podAnnotations }}
{{ toYaml .Values.global.podAnnotations }}
{{- end }}
{{- end }}