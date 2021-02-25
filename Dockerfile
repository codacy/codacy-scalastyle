FROM openjdk:8-jre-alpine
RUN apk add bash
COPY docs /docs
COPY target/universal/stage/ /opt/docker/
RUN adduser -u 2004 -D docker && chmod +x /opt/docker/bin/codacy-scalastyle
USER docker
WORKDIR /src
ENTRYPOINT ["/opt/docker/bin/codacy-scalastyle"]
