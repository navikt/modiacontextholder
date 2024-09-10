val ktor_version = "2.3.12"
val kotlin_version = "2.0.20"

val modia_common_version = "1.2024.09.09-09.18-1e1cb34aaec3"
val nav_common_version = "3.2024.02.21_11.18-8f9b43befae1"
val graphql_kotlin_version = "8.0.0"
val caffeine_version = "3.1.8"
val unleash_version = "9.2.4"
val okhttp3_version = "4.12.0"
val mockk_version = "1.13.12"
val testcontainers_version = "1.20.1"

group = "no.nav"
version = "1.0.0-SNAPSHOT"
description = "modiacontextholder"

plugins {
    kotlin("jvm") version "2.0.20"
    id("io.ktor.plugin") version "2.3.12"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    idea
}

application {
    mainClass.set("no.nav.modiacontextholder.ApplicationKt")

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
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-server-serialization-ktor:$ktor_version")
    implementation("io.ktor:ktor-server-cors:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-websockets:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    implementation("io.getunleash:unleash-client-java:$unleash_version")
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
    implementation("com.github.ben-manes.caffeine:caffeine:$caffeine_version")

    api("com.squareup.okhttp3:okhttp:$okhttp3_version")
    // api(libs.io.micrometer.micrometer.registry.prometheus)
    // api(libs.io.lettuce.lettuce.core)
    // api(libs.org.jetbrains.kotlin.kotlin.stdlib)
    // api(libs.org.jetbrains.kotlin.kotlin.stdlib.jdk8)
    // api(libs.org.jetbrains.kotlin.kotlin.reflect)
    implementation("com.expediagroup:graphql-kotlin-client-jackson:$graphql_kotlin_version")
    implementation("com.expediagroup:graphql-kotlin-ktor-client:$graphql_kotlin_version")

    implementation("io.ktor:ktor-client-core-jvm:2.3.12")

    testImplementation("no.nav.common:test:$nav_common_version")
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
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks {
    shadowJar {
        archiveBaseName.set("app")
    }

    build {
        dependsOn("shadowJar")
    }
}
