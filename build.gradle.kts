val mainClass = "no.nav.modiacontextholder.MainKt"

group = "no.nav"
version = "1.0-SNAPSHOT"
description = "modiacontextholder"

plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.3.3"
    id("org.spring.dependency-management") version "1.1.6"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    idea
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }

    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
}

dependencies {
    implementation(libs.io.vavr.vavr)
    implementation(libs.io.getunleash.unleash.client.java)
    api(libs.com.github.navikt.modia.common.utils.logging)
    api(libs.no.nav.common.util)
    api(libs.no.nav.common.nais)
    api(libs.no.nav.common.auth)
    api(libs.no.nav.common.rest)
    api(libs.no.nav.common.client)
    api(libs.no.nav.common.token.client)
    api(libs.com.squareup.okhttp3.okhttp)
    api(libs.org.springframework.boot.spring.boot.starter)
    api(libs.org.springframework.boot.spring.boot.starter.web)
    api(libs.org.springframework.boot.spring.boot.starter.websocket)
    api(libs.org.springframework.boot.spring.boot.starter.cache)
    api(libs.org.springframework.boot.spring.boot.starter.test)
    api(libs.org.springframework.boot.spring.boot.starter.actuator)
    api(libs.org.springframework.boot.spring.boot.starter.aop)
    api(libs.io.micrometer.micrometer.registry.prometheus)
    api(libs.com.github.ben.manes.caffeine.caffeine)
    api(libs.io.lettuce.lettuce.core)
    api(libs.org.jetbrains.kotlin.kotlin.stdlib)
    api(libs.org.jetbrains.kotlin.kotlin.stdlib.jdk8)
    api(libs.org.jetbrains.kotlin.kotlin.reflect)
    api(libs.com.expediagroup.graphql.kotlin.ktor.client)
    api(libs.com.expediagroup.graphql.kotlin.client.jackson)
    api(libs.com.fasterxml.jackson.module.jackson.module.kotlin)
    api(libs.org.jetbrains.kotlinx.kotlinx.serialization.core)
    api(libs.org.jetbrains.kotlinx.kotlinx.serialization.json)
    testImplementation(libs.no.nav.common.test)
    testImplementation(libs.org.mockito.mockito.core)
    testImplementation(libs.org.assertj.assertj.core)
    testImplementation(libs.org.junit.jupiter.junit.jupiter.api)
    testImplementation(libs.org.testcontainers.junit.jupiter)
    testImplementation(libs.org.testcontainers.testcontainers)
    testImplementation(libs.com.squareup.okhttp3.mockwebserver)
    testImplementation(libs.io.mockk.mockk.jvm)
    testImplementation(libs.io.ktor.ktor.client.mock.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
}

tasks {
    shadowJar {
        archiveBaseName.set("app")
    }

    build {
        dependsOn("shadowJar")
    }
}
