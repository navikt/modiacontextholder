import com.expediagroup.graphql.plugin.gradle.config.GraphQLSerializer
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLDownloadSDLTask
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLGenerateClientTask

val ktor_version = "3.6.1"
val kotlin_version = "2.0.20"
val koin_version = "4.1.0-Beta8"

val modia_common_version = "1.2026.09.02-15.52-e0484649b92e"
val nav_common_version = "4.2026.11.05_07.24-dd0ce2805c16"
val graphql_kotlin_version = "11.0.0"
val caffeine_version = "3.3.1"
val unleash_version = "11.2.1"
val okhttp3_version = "5.5.1"
val mockk_version = "1.14.12"
val testcontainers_version = "1.21.4"
val vavr_version = "1.1.0"
val lettuce_version = "7.6.0.RELEASE"
val kotlinx_serialization_version = "1.12.0"
val kotlinx_datetime_version = "0.8.0"
val kotlinx_coroutines_version = "1.11.0"
val assertj_version = "3.27.7"
val logstash_logback_enocder_version = "9.0"

val mainClassName = "no.nav.modiacontextholder.MainKt"

plugins {
    kotlin("jvm") version "2.4.20"
    application
    kotlin("plugin.serialization") version "2.4.20"
    id("com.expediagroup.graphql") version "11.0.0"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
}

dependencyLocking {
    lockAllConfigurations()
}

dependencies {
    listOf("implementation", "graphqlClient", "graphqlGraalVM", "graphqlSDL").forEach { configuration ->
        add(configuration, platform("com.fasterxml.jackson:jackson-bom:2.23.0"))
        add(configuration, platform("tools.jackson:jackson-bom:3.3.0"))
    }
    implementation(platform("io.netty:netty-bom:4.2.20.Final"))
    implementation(platform("io.micrometer:micrometer-bom:1.17.2"))

    constraints {
        implementation("com.google.guava:guava:33.8.0-jre")
        testImplementation("org.apache.commons:commons-compress:1.28.0")
    }

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
    implementation("io.lettuce:lettuce-core:$lettuce_version")

    implementation("com.squareup.okhttp3:okhttp:$okhttp3_version")
    implementation("io.vavr:vavr:$vavr_version")
    implementation("com.expediagroup:graphql-kotlin-ktor-client:$graphql_kotlin_version")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstash_logback_enocder_version")

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
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktor_version")
    testImplementation("io.insert-koin:koin-test-junit5:$koin_version")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

sourceSets {
    main {
        kotlin {
            srcDir(layout.buildDirectory.dir("generated/source/graphql/main"))
        }
    }
}

tasks.named<GraphQLDownloadSDLTask>("graphqlDownloadSDL") {
    endpoint.set("https://navikt.github.io/pdl/pdl-api-sdl.graphqls")
}

tasks.named<GraphQLGenerateClientTask>("graphqlGenerateClient") {
    packageName.set("no.nav.modiacontextholder.consumers.pdl.generated")
    schemaFile.set(tasks.named<GraphQLDownloadSDLTask>("graphqlDownloadSDL").flatMap { it.outputFile })
    queryFileDirectory.set(file("${project.projectDir}/src/main/resources/pdl/queries"))
    serializer.set(GraphQLSerializer.KOTLINX)

    dependsOn("graphqlDownloadSDL")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(mapOf("Main-Class" to mainClassName))
    }
    dependsOn("graphqlGenerateClient")
}
