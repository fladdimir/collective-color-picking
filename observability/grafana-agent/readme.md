# grafana-agent

for scraping and forwarding prometheus-metrics, opentelemetry-traces, and syslog-logs

[agent-config](./agent.yml) with environment variable placeholders for flexible configuration of endpoints and credentials

see [local.env] for sample local settings, working the [local observability setup](../local/docker-compose.yml)

```sh
# download the binary
curl -o "agent-linux-amd64.zip" -L "https://github.com/grafana/agent/releases/download/v0.22.0/agent-linux-amd64.zip"

# extract the binary
unzip "agent-linux-amd64.zip"

# make sure it is executable
chmod a+x "agent-linux-amd64"

./agent-linux-amd64 -config.expand-env -config.file=./agent.yml -prometheus.wal-directory=./tmp

```
