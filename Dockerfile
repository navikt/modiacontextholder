FROM gradle:8-jdk21-alpine as builder

# sett riktig tidssone
ENV TZ Europe/Oslo
RUN ln -fs /usr/share/zoneinfo/Europe/Oslo /etc/localtime

ADD / /source
WORKDIR /source
RUN gradle build -x test

FROM gcr.io/distroless/java21-debian12

COPY --from=builder /source/build/libs/modiacontextholder-all.jar app.jar

USER nonroot

CMD ["app.jar"]
