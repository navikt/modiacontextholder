FROM gcr.io/distroless/java21-debian13

COPY build/install/*/lib /lib

USER nonroot

ENTRYPOINT ["java", "-cp", "/lib/*", "no.nav.modiacontextholder.MainKt"]
