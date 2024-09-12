import com.expediagroup.graphql.plugin.gradle.config.GraphQLSerializer
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLDownloadSDLTask
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLGenerateClientTask

val ktor_version = "2.3.12"
val kotlin_version = "2.0.20"
val koin_version = "3.5.6"

val modia_common_version = "1.2024.09.09-09.18-1e1cb34aaec3"
val nav_common_version = "3.2024.05.23_05.46-2b29fa343e8e"
val graphql_kotlin_version = "8.0.0"
val caffeine_version = "3.1.8"
val unleash_version = "9.2.4"
val okhttp3_version = "4.12.0"
val mockk_version = "1.13.12"
val testcontainers_version = "1.20.1"
val vavr_version = "0.10.4"
val jedis_version = "5.1.4"
val kotlinx_serialization_version = "1.7.1"

group = "no.nav"
version = "1.0.0-SNAPSHOT"
description = "modiacontextholder"

plugins {
    kotlin("jvm") version "2.0.20"
    id("io.ktor.plugin") version "2.3.12"
    kotlin("plugin.serialization") version "2.0.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.expediagroup.graphql") version "8.0.0"
    idea
}

application {
    mainClass.set("no.nav.modiacontextholder.MainKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinx_serialization_version")

    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-cors:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("io.ktor:ktor-server-websockets:$ktor_version")
    implementation("io.ktor:ktor-client-core-jvm:$ktor_version")

    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")

    implementation("com.github.navikt.modia-common-utils:logging:$modia_common_version")
    implementation("com.github.navikt.modia-common-utils:logging:$modia_common_version")
    implementation("com.github.navikt.modia-common-utils:kotlin-utils:$modia_common_version")
    implementation("com.github.navikt.modia-common-utils:ktor-utils:$modia_common_version")

    implementation("no.nav.common:auth:$nav_common_version")
    implementation("no.nav.common:client:$nav_common_version")
    implementation("no.nav.common:nais:$nav_common_version")
    implementation("no.nav.common:rest:$nav_common_version")
    implementation("no.nav.common:token-client:$nav_common_version")
    implementation("no.nav.common:util:$nav_common_version")

    implementation("io.getunleash:unleash-client-java:$unleash_version")
    implementation("com.github.ben-manes.caffeine:caffeine:$caffeine_version")
    implementation("redis.clients:jedis:$jedis_version")

    implementation("com.squareup.okhttp3:okhttp:$okhttp3_version")
    implementation("io.vavr:vavr:$vavr_version")
    // api(libs.io.micrometer.micrometer.registry.prometheus)
    // api(libs.io.lettuce.lettuce.core)
    // api(libs.org.jetbrains.kotlin.kotlin.stdlib)
    // api(libs.org.jetbrains.kotlin.kotlin.stdlib.jdk8)
    // api(libs.org.jetbrains.kotlin.kotlin.reflect)
    implementation("com.expediagroup:graphql-kotlin-client-jackson:$graphql_kotlin_version")
    implementation("com.expediagroup:graphql-kotlin-ktor-client:$graphql_kotlin_version")

    // testImplementation(libs.org.mockito.mockito.core)
    // testImplementation(libs.org.assertj.assertj.core)
    // testImplementation(libs.org.junit.jupiter.junit.jupiter.api)
    testImplementation("org.testcontainers:testcontainers:$testcontainers_version")
    // testImplementation(libs.com.squareup.okhttp3.mockwebserver)
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("io.mockk:mockk-jvm:$mockk_version")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

val graphqlDownloadSDL by tasks.getting(GraphQLDownloadSDLTask::class) {
    endpoint.set("https://navikt.github.io/pdl/pdl-api-sdl.graphqls")
}

val graphqlGenerateClient by tasks.getting(GraphQLGenerateClientTask::class) {
    packageName.set("no.nav.modiacontextholder.consumers.pdl.generated")
    schemaFile.set(graphqlDownloadSDL.outputFile)
    queryFileDirectory.set(file("${project.projectDir}/src/main/resources/pdl/queries"))
    serializer.set(GraphQLSerializer.KOTLINX)

    dependsOn("graphqlDownloadSDL")
}

tasks {
    shadowJar {
        archiveBaseName.set("app")
        dependsOn("graphqlGenerateClient")
    }

    build {
        dependsOn("shadowJar")
    }
}
