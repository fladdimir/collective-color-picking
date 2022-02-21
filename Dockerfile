# gradle native image build
FROM quay.io/quarkus/ubi-quarkus-native-image:21.3.1-java11 AS be_build
COPY --chown=quarkus:quarkus be_hwp/gradlew /code/gradlew
COPY --chown=quarkus:quarkus be_hwp/gradle /code/gradle
COPY --chown=quarkus:quarkus be_hwp/build.gradle /code/
COPY --chown=quarkus:quarkus be_hwp/settings.gradle /code/
COPY --chown=quarkus:quarkus be_hwp/gradle.properties /code/
USER quarkus
WORKDIR /code
COPY be_hwp/src /code/src
RUN ./gradlew assemble --info
RUN ./gradlew assemble --info -Dquarkus.package.type=native

# build fe app
FROM node:lts-alpine as fe_build
WORKDIR /app
COPY fe_hwp/package*.json ./
RUN npm install
COPY fe_hwp .
RUN npm run build

# download grafana agent
FROM debian:latest as grafana_download
RUN apt-get update
RUN apt-get install curl unzip -y
COPY observability/grafana-agent/SHA256SUMS SHA256SUMS
RUN curl -o "agent-linux-amd64.zip" -L "https://github.com/grafana/agent/releases/download/v0.23.0/agent-linux-amd64.zip"
RUN sha256sum -c SHA256SUMS --status --strict
RUN unzip "agent-linux-amd64.zip"

# heroku image
FROM nginx
# rsyslog
RUN apt-get update
RUN apt-get install rsyslog -y
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
COPY --from=grafana_download /agent-linux-amd64 agent-linux-amd64
RUN chmod a+x agent-linux-amd64
COPY observability/grafana-agent/agent.yml agent.yml
# nginx config
COPY nginx/etc_nginx/ /etc/nginx/
# fe build
COPY --from=fe_build app/dist/ /usr/share/nginx/html/
# be build
COPY --from=be_build /code/build/*-runner application

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
