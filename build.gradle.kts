import com.expediagroup.graphql.plugin.gradle.config.GraphQLSerializer
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLDownloadSDLTask
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLGenerateClientTask

val ktor_version = "3.0.1"
val kotlin_version = "2.0.20"
val koin_version = "4.1.0-Beta1"

val modia_common_version = "1.2024.11.28-09.12-c4f9d4c27afb"
val nav_common_version = "3.2024.11.26_16.35-432a29107830"
val graphql_kotlin_version = "8.2.1"
val caffeine_version = "3.1.8"
val unleash_version = "9.2.6"
val okhttp3_version = "4.12.0"
val mockk_version = "1.13.13"
val testcontainers_version = "1.20.4"
val vavr_version = "0.10.5"
val jedis_version = "5.2.0"
val lettuce_version = "6.5.1.RELEASE"
val kotlinx_serialization_version = "1.7.3"
val kotlinx_datetime_version = "0.6.1"
val kotlinx_coroutines_version = "1.9.0"
val assertj_version = "3.26.3"

val mainClass = "no.nav.modiacontextholder.MainKt"

plugins {
    kotlin("jvm") version "2.1.0"
    id("io.ktor.plugin") version "3.0.2"
    kotlin("plugin.serialization") version "2.1.0"
    id("com.gradleup.shadow") version "8.3.5"
    id("com.expediagroup.graphql") version "8.2.1"
}

project.setProperty("mainClassName", mainClass)

repositories {
    mavenCentral()
    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinx_serialization_version")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinx_datetime_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinx_coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinx_coroutines_version")

    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-server-swagger:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-cors:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("io.ktor:ktor-server-websockets:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging:$ktor_version")
    implementation("io.ktor:ktor-client-core-jvm:$ktor_version")

    implementation("io.insert-koin:koin-ktor3:$koin_version")
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
    implementation("io.lettuce:lettuce-core:$lettuce_version")

    implementation("com.squareup.okhttp3:okhttp:$okhttp3_version")
    implementation("io.vavr:vavr:$vavr_version")
    implementation("com.expediagroup:graphql-kotlin-client-jackson:$graphql_kotlin_version")
    implementation("com.expediagroup:graphql-kotlin-ktor-client:$graphql_kotlin_version")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktor_version")
    testImplementation("io.ktor:ktor-client-core:$ktor_version")
    testImplementation("io.ktor:ktor-client-okhttp:$ktor_version")
    testImplementation("org.testcontainers:testcontainers:$testcontainers_version")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainers_version")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttp3_version")
    testImplementation("io.mockk:mockk-jvm:$mockk_version")
    testImplementation("org.assertj:assertj-core:$assertj_version")
    testImplementation("io.ktor:ktor-server-test-host-jvm:2.3.12")
    testImplementation("io.insert-koin:koin-test-junit5:$koin_version")
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

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    enabled = false
}

tasks.shadowJar {
    archiveFileName.set("modiacontextholder.jar")
    manifest {
        attributes(
            mapOf("Main-Class" to mainClass),
        )
    }
    dependsOn("graphqlGenerateClient")
}

tasks.distZip {
    dependsOn("shadowJar")
}

tasks.distTar {
    dependsOn("shadowJar")
}

tasks.startScripts {
    dependsOn("shadowJar")
}

tasks.build {
    dependsOn("shadowJar")
}
