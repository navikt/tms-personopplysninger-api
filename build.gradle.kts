import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    kotlin("jvm").version(Kotlin.version)

    id(GraphQL.pluginId) version GraphQL.version

    id(TmsJarBundling.plugin)

    // Apply the application plugin to add support for building a CLI application.
    application
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven")
    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
    mavenLocal()
}

dependencies {
    implementation(Caffeine.caffeine)
    implementation(GraphQL.kotlinClient)
    implementation(GraphQL.kotlinKtorClient)
    implementation(JacksonDatatype.datatypeJsr310)
    implementation(JacksonDatatype.moduleKotlin)
    implementation(Kafka.clients)
    implementation(KotliQuery.kotliquery)
    implementation(KotlinLogging.logging)
    implementation(Ktor.Server.contentNegotiation)
    implementation(Ktor.Server.core)
    implementation(Ktor.Server.defaultHeaders)
    implementation(Ktor.Server.netty)
    implementation(Ktor.Server.htmlDsl)
    implementation(Ktor.Server.statusPages)
    implementation(Ktor.Server.auth)
    implementation(Ktor.Client.contentNegotiation)
    implementation(Ktor.Client.apache)
    implementation(Ktor.Serialization.jackson)
    implementation(Logstash.logbackEncoder)
    implementation(Postgresql.postgresql)
    implementation(TmsKtorTokenSupport.azureExchange)
    implementation(TmsKtorTokenSupport.tokenXValidation)
    implementation(TmsKtorTokenSupport.tokendingsExchange)
    implementation(TmsKtorTokenSupport.idportenSidecar)
    implementation(TmsCommonLib.utils)
    implementation(TmsCommonLib.kubernetes)
    implementation(TmsCommonLib.observability)
    implementation(TmsCommonLib.metrics)
    implementation(TmsKafkaTools.kafkaApplication)
    implementation(Prometheus.metricsCore)

    testImplementation(JunitPlatform.launcher)
    testImplementation(JunitJupiter.api)
    testImplementation(Mockk.mockk)
    testImplementation(TestContainers.postgresql)
    testImplementation(Kotest.runnerJunit5)
    testImplementation(Kotest.assertionsCore)
}

application {
    mainClass.set("no.nav.tms.personopplysninger.api")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events("passed", "skipped", "failed")
        }
    }
}

graphql {
    client {
        sdlEndpoint = "https://navikt.github.io/pdl/pdl-api-sdl.graphqls"
        packageName = "no.nav.pdl.generated.dto"
        queryFileDirectory = "${project.projectDir.absolutePath}/src/main/resources/graphql"
    }
}
