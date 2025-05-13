package no.nav.tms.personopplysninger.api.institusjon

import com.fasterxml.jackson.databind.JsonNode
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tms.personopplysninger.api.InternalRouteConfig
import no.nav.tms.personopplysninger.api.RouteTest
import no.nav.tms.personopplysninger.api.common.HeaderHelper
import no.nav.tms.personopplysninger.api.common.TokenExchanger
import no.nav.tms.personopplysninger.api.kontoregister.KontoResponse
import no.nav.tms.personopplysninger.api.routeConfig
import org.junit.jupiter.api.Test

class HentInstitusjonsoppholdRouteTest: RouteTest() {

    private val inst2Url = "http://inst2"
    private val inst2Token = "<inst2-token>"

    private val tokenExchanger: TokenExchanger = mockk<TokenExchanger>().also {
        coEvery { it.inst2Token(any()) } returns inst2Token
    }

    private val internalRouteConfig: InternalRouteConfig = { client ->
        routeConfig {
            institusjonRoutes(
                InstitusjonConsumer(client, inst2Url, tokenExchanger)
            )
        }
    }

    private val hentInstitusjonsoppholdPath = "/institusjonsopphold"

    private val externalResponse = """
[
      {
        "organisasjonsnummer": "00123456",
        "institusjonsnavn": "BORTEHJEMMET AS",
        "institusjonstype": "AS",
        "startdato": "2025-01-01",
        "faktiskSluttdato": "2025-02-01",
        "fiktivSluttdato": null,
        "registreringstidspunkt": "2025-01-01T12:00:00.000"
      }
]
    """

    @Test
    fun `henter institusjonsopphold via inst2`() = apiTest(internalRouteConfig) {client ->

        var capturedPayload: JsonNode? = null

        externalService(inst2Url) {
            post("/rest/v1/innsyn") {
                capturedPayload = call.receiveJson()
                call.respondText(externalResponse, ContentType.Application.Json)
            }
        }

        val response = client.get(hentInstitusjonsoppholdPath)

        response.status shouldBe HttpStatusCode.OK
        response.json().first().let {
            it["organisasjonsnummer"].asText() shouldBe "00123456"
            it["institusjonsnavn"].asText() shouldBe "BORTEHJEMMET AS"
            it["institusjonstype"].asText() shouldBe "Alders- og sykehjem"
            it["startdato"].asText() shouldBe "2025-01-01"
            it["faktiskSluttdato"].asText() shouldBe "2025-02-01"
            it["fiktivSluttdato"].asTextOrNull() shouldBe null
            it["registreringstidspunkt"].asText() shouldBe "2025-01-01T12:00:00"
        }

        capturedPayload.shouldNotBeNull().let {
            it["personident"].asText() shouldBe testIdent
        }
    }

    @Test
    fun `bruker riktige headers mot inst2`() = apiTest(internalRouteConfig) { client ->
        var headers: Headers? = null

        externalService(inst2Url) {
            post("/rest/v1/innsyn") {
                headers = call.request.headers
                call.respond(emptyList<KontoResponse.Landkode>())
            }
        }

        client.get(hentInstitusjonsoppholdPath)

        headers.shouldNotBeNull().let {
            it[HttpHeaders.Authorization] shouldBe "Bearer $inst2Token"
            it[HeaderHelper.NAV_CONSUMER_ID_HEADER] shouldBe HeaderHelper.NAV_CONSUMER_ID
            it[HeaderHelper.CALL_ID_HEADER].shouldNotBeNull()
        }
    }

    @Test
    fun `svarer med InternalServerError ved feil mot inst2`() = apiTest(internalRouteConfig) {client ->
        externalService(inst2Url) {
            post("/rest/v1/innsyn") {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        val response = client.get(hentInstitusjonsoppholdPath)

        response.status shouldBe HttpStatusCode.InternalServerError
    }
}
