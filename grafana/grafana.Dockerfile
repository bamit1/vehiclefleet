FROM grafana/grafana-enterprise:latest


ENV GF_SECURITY_ADMIN_PASSWORD=password
ENV GF_SECURITY_ALLOW_EMBEDDING=true
ENV GF_AUTH_ANONYMOUS_ENABLED=true
ENV GF_AUTH_ANONYMOUS_ORG_ROLE=Admin
ENV GF_INSTALL_PLUGINS=grafana-clickhouse-datasource

EXPOSE 3000