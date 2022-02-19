FROM nginx
# rsyslog
RUN apt-get update && apt-get install rsyslog -y
COPY nginx/etc_nginx/rsyslog-udp-tcp.conf /etc/rsyslog.conf
# app dir
RUN mkdir /opt/app
WORKDIR /opt/app
RUN mkdir tmp
# helper scripts
COPY deploy/ ./
RUN chmod a+x ./wait-for-it.sh
RUN chmod a+x ./heroku/prepare_jdbc_url.sh
# grafana-agent + config
COPY observability/grafana-agent/agent-linux-amd64 agent-linux-amd64
RUN chmod a+x agent-linux-amd64
COPY observability/grafana-agent/agent.yml agent.yml
# nginx config
COPY nginx/etc_nginx/ /etc/nginx/
# fe build
COPY fe_hwp/dist/ /usr/share/nginx/html/
# be build
COPY be_hwp/build/*-runner application

CMD ["/bin/bash", "-c", \
    "service rsyslog start \
    \
    & ./agent-linux-amd64 -config.expand-env -config.file=./agent.yml -prometheus.wal-directory=./tmp \
    \
    & ( ./wait-for-it.sh 127.0.0.1:4316 && envsubst < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf && nginx -g 'daemon off;' ) \
    \
    & ( \
    . ./heroku/prepare_jdbc_url.sh \
    && ./wait-for-it.sh 127.0.0.1:4316 \
    && /opt/app/application -Xmx256m \
    ) \
    "]
