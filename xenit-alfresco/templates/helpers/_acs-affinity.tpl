{{- define "acs.affinity" -}}
{{- $affinity := .Values.acs.affinity | default dict | deepCopy -}}

{{- /*If acs.hostnameAntiAffinity flag is enabled, it needs to be merged into the full set of affinity rules*/ -}}
{{- if .Values.acs.hostnameAntiAffinity.enabled -}}
    {{- $antiAffinityRule := 
        dict "labelSelector" (
            dict "matchLabels" (
                dict "app" "acs"
            )
        ) 
        "topologyKey" "kubernetes.io/hostname"
    -}}

    {{- $podAntiAffinity := $affinity.podAntiAffinity | default dict -}}
    {{- $existingRules := $podAntiAffinity.requiredDuringSchedulingIgnoredDuringExecution | default list -}}
    {{- $mergedRules := append $existingRules $antiAffinityRule -}}
    {{- $newPodAntiAffinity := merge (dict "requiredDuringSchedulingIgnoredDuringExecution" $mergedRules) $podAntiAffinity -}}
    {{- $affinity = merge (dict "podAntiAffinity" $newPodAntiAffinity) $affinity -}}
{{- end -}}

{{- toYaml $affinity }}

{{- end -}}