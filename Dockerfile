# Build stage
FROM maven:3.9.9-eclipse-temurin-21 as build

COPY src /home/app/src
COPY pom.xml /home/app
COPY settings.xml /root/.m2/settings.xml
COPY docker /home/app/docker

RUN mvn -f /home/app/pom.xml clean package -DskipTests

# Optimize stage
FROM eclipse-temurin:21-jdk-alpine AS optimize

COPY --from=build /home/app/target/*.jar /app/app.jar

WORKDIR /app

# List jar modules
RUN jar xf app.jar
RUN jdeps \
    --ignore-missing-deps \
    --print-module-deps \
    --multi-release 21 \
    --recursive \
    --class-path 'BOOT-INF/lib/*' \
    app.jar > modules.txt

# Create a custom Java runtime
RUN $JAVA_HOME/bin/jlink \
         --add-modules $(cat modules.txt) \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /javaruntime

# Package stage
FROM alpine:latest

ENV JAVA_HOME=/opt/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# copy optimized JRE
COPY --from=optimize /javaruntime $JAVA_HOME

LABEL org.opencontainers.image.authors="CZERTAINLY <support@czertainly.com>"

# add non root user czertainly
RUN addgroup --system --gid 10001 czertainly && adduser --system --home /opt/czertainly --uid 10001 --ingroup czertainly czertainly

COPY --from=build /home/app/docker /
COPY --from=build /home/app/target/*.jar /opt/czertainly/app.jar

WORKDIR /opt/czertainly

ENV JDBC_URL=
ENV JDBC_USERNAME=
ENV JDBC_PASSWORD=
ENV DB_SCHEMA=webhooknp
ENV PORT=8080
ENV JAVA_OPTS=

USER 10001

ENTRYPOINT ["/opt/czertainly/entry.sh"]
