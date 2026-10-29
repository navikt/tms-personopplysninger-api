import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm").version(libs.versions.kotlin)

    alias(libs.plugins.graphql)

    alias(libs.plugins.tms.jarBundling)

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
    implementation(libs.bundles.ktorClient)
    implementation(libs.bundles.ktorServer)
    implementation(libs.bundles.jackson)
    implementation(libs.bundles.logback)
    implementation(libs.bundles.graphql)

    implementation(libs.caffeine)
    implementation(libs.kotlinLogging)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.defaultHeaders)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.tms.ktorTokenSupport.entraId.fetcher)
    implementation(libs.tms.ktorTokenSupport.userToken.verification)
    implementation(libs.tms.ktorTokenSupport.userToken.exchange)
    implementation(libs.tms.commonLib.metrics)
    implementation(libs.tms.commonLib.teamLogger)
    implementation(libs.tms.commonLib.utils)
    implementation(libs.prometheus)

    testImplementation(libs.bundles.junit)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.mockk)
    testImplementation(libs.ktor.test.clientMock)
    testImplementation(libs.ktor.test.server)
    testImplementation(libs.tms.ktorTokenSupport.userToken.verificationMock)
}

application {
    mainClass.set("no.nav.tms.personopplysninger.api.ApplicationKt")
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
