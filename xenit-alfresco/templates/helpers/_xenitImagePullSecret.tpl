{{- define "privateImagePullSecret" }}
{{- with .Values.imageCredentials.private }}
{{- printf "{\"auths\":{\"%s\":{\"username\":\"%s\",\"password\":\"%s\",\"email\":\"%s\",\"auth\":\"%s\"}}}" .registry (required "please pass a useraname to the private image repo to your helm installation via the value imageCredentials.private.username" .username) (required "please pass a password to the private image repo to your helm installation via the value imageCredentials.private.password" .password) .email (printf "%s:%s" .username .password | b64enc) | b64enc }}
{{- end }}
{{- end }}