package no.nav.tms.personopplysninger.api

import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.junit.jupiter.api.Test

class CorsTest : RouteTest() {

    private val testPath = "/cors"

    private val allowedSchemes = "http"
    private val allowedOrigins = "*.test.no"

    private val internalRouteConfig: InternalRouteConfig = { client ->
        routeConfig {
            get(testPath) {
                call.respond(HttpStatusCode.OK)
            }
        }
    }

    @Test
    fun `slipper gjennom kall fra godkjente kilder`() = apiTest(
        internalRouteConfig = internalRouteConfig,
        corsAllowedOrigins = allowedOrigins,
        corsAllowedSchemes = allowedSchemes
    ) {client ->
        val response = client.get(testPath) {
            header(HttpHeaders.Origin, "http://subdomain.test.no")
        }

        response.status shouldBe HttpStatusCode.OK
        response.headers[HttpHeaders.AccessControlAllowOrigin] shouldBe "http://subdomain.test.no"
    }

    @Test
    fun `slipper ikke gjennom kall fra ukjente kilder`() = apiTest(
        internalRouteConfig = internalRouteConfig,
        corsAllowedOrigins = allowedOrigins,
        corsAllowedSchemes = allowedSchemes
    ) {client ->
        val response = client.get(testPath) {
            header(HttpHeaders.Origin, "http://ukjent.no")
        }

        response.status shouldBe HttpStatusCode.Forbidden
    }

    @Test
    fun `slipper ikke gjennom kall fra feil protokoll`() = apiTest(
        internalRouteConfig = internalRouteConfig,
        corsAllowedOrigins = allowedOrigins,
        corsAllowedSchemes = allowedSchemes
    ) {client ->
        val response = client.get(testPath) {
            header(HttpHeaders.Origin, "https://test.no")
        }

        response.status shouldBe HttpStatusCode.Forbidden
    }
}
