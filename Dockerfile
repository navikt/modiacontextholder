FROM maven:3.9.6-eclipse-temurin-17-alpine as builder

# sett riktig tidssone
ENV TZ Europe/Oslo
RUN ln -fs /usr/share/zoneinfo/Europe/Oslo /etc/localtime

ADD / /source
WORKDIR /source
RUN mvn package -DskipTests

#FROM gcr.io/distroless/java17-debian12:nonroot
FROM eclipse-temurin:17-alpine

WORKDIR /app

COPY --from=builder /source/target/modiacontextholder.jar /app/app.jar

CMD ["java", "-jar", "/app/app.jar"]
