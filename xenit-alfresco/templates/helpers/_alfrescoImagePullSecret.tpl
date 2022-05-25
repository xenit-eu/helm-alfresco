{{- define "alfrescoImagePullSecret" }}
{{- with .Values.imageCredentials.alfresco }}
{{- printf "{\"auths\":{\"%s\":{\"username\":\"%s\",\"password\":\"%s\",\"email\":\"%s\",\"auth\":\"%s\"}}}" .registry (required "please pass a username to the alfresco (quay) image repo to your helm installation via the value imageCredentials.alfresco.username" .username) (required "please pass a password to the quay image repo to your helm installation via the value imageCredentials.alfresco.password" .password) .email (printf "%s:%s" .username .password | b64enc) | b64enc }}
{{- end }}
{{- end }}