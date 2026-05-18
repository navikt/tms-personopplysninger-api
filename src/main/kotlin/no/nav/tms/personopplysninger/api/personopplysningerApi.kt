package no.nav.tms.personopplysninger.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.common.logging.TeamLogs
import no.nav.tms.common.metrics.installTmsMicrometerMetrics
import no.nav.tms.personopplysninger.api.common.ConsumerException
import no.nav.tms.personopplysninger.api.personalia.SlettPersonopplysningException
import no.nav.tms.token.support.user.token.verification.UserPrincipal
import no.nav.tms.token.support.user.token.verification.userToken
import java.lang.IllegalStateException

fun Application.mainModule(
    userRoutes: Route.() -> Unit,
    httpClient: HttpClient,
    corsAllowedOrigins: String,
    corsAllowedSchemes: String,
    authInstaller: Application.() -> Unit = {
        authentication {
            userToken {

            }
        }
    }
) {
    val log = KotlinLogging.logger {}
    val teamLog = TeamLogs.logger { }

    authInstaller()

    install(DefaultHeaders)

    install(CORS) {
        allowHost(host = corsAllowedOrigins, schemes = listOf(corsAllowedSchemes))
        allowCredentials = true
        allowHeader(HttpHeaders.ContentType)
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when(cause) {
                is ConsumerException -> {
                    log.error { "Kall mot ${cause.externalService} [${cause.endpoint}] feiler med kode [${cause.status}]" }
                    teamLog.error { "Kall mot krr-proxy [${cause.endpoint}] feiler med kode [${cause.status}] og melding: ${cause.responseContent}" }
                    call.respond(HttpStatusCode.InternalServerError)
                }
                is SlettPersonopplysningException -> {
                    log.error(cause) { "Sletting av personopplysning feilet" }
                    call.respond(HttpStatusCode.UnprocessableEntity)
                }
                else -> {
                    teamLog.warn(cause) { "Kall til ${call.request.uri} feilet: ${cause.message}" }
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
    }

    install(ContentNegotiation) {
        jackson { jsonConfig() }
    }

    installTmsMicrometerMetrics {
        setupMetricsRoute = true
        installMicrometerPlugin = true
    }

    routing {
        metaRoutes()
        authenticate {
            userRoutes()
        }
    }

    configureShutdownHook(httpClient)
}

private fun Route.metaRoutes() {
    get("/internal/isalive") {
        call.respondText(text = "ALIVE", contentType = ContentType.Text.Plain)
    }

    get("/internal/isready") {
        call.respondText(text = "READY", contentType = ContentType.Text.Plain)
    }
}

private fun Application.configureShutdownHook(httpClient: HttpClient) {
    monitor.subscribe(ApplicationStopping) {
        httpClient.close()
    }
}

fun ObjectMapper.jsonConfig(): ObjectMapper {
    registerKotlinModule()
    registerModule(JavaTimeModule())
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    return this
}

val ApplicationCall.user get() = principal<UserPrincipal>() ?: throw IllegalStateException("Fant ikke UserPrincipal i context")
