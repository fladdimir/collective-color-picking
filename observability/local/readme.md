# local observability stack for metrics, logs, traces

provide local grafana-cloud-like api, ready to receive push data from grafana-agent

## metrics

prometheus with enabled remote-write-receiver

## logs

loki + grafana-frontend

## traces

otel-collector + jaeger all-in-one for storage+visualization
