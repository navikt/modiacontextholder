FROM maven:3.6.1-jdk-8-alpine as builder

# sett riktig tidssone
ENV TZ Europe/Oslo
RUN ln -fs /usr/share/zoneinfo/Europe/Oslo /etc/localtime

ADD / /source
WORKDIR /source
RUN mvn package -DskipTests

FROM navikt/java:8-appdynamics
ENV APPD_ENABLED=true
COPY java-debug.sh /init-scripts/08-java-debug.sh
COPY --from=builder /source/target/modiacontextholder /app
