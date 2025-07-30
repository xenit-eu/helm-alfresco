{{- define "allHostNames" }}
{{- $allHostNames := .Values.ingress.hosts | default (list) }}
{{- if .Values.ingress.host }}
  {{- $allHostNames = append $allHostNames .Values.ingress.host }}
{{- end }}
{{- if eq (len $allHostNames) 0 }}
  {{- fail "No hostname specified. Please specify the 'values.ingress.hosts' property (list of hostnames)." }}
{{- end }}
{{- toYaml $allHostNames }}
{{- end }}