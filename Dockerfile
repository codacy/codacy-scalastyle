FROM amazoncorretto:8-alpine3.17-jre
RUN apk add bash
COPY docs /docs
COPY target/universal/stage/ /opt/docker/
RUN adduser -u 2004 -D docker && chmod +x /opt/docker/bin/codacy-scalastyle
USER docker
WORKDIR /src
ENTRYPOINT ["/opt/docker/bin/codacy-scalastyle"]
