package no.nav.tms.personopplysninger.api.kodeverk

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tms.personopplysninger.api.RouteTest
import no.nav.tms.personopplysninger.api.InternalRouteConfig
import no.nav.tms.personopplysninger.api.common.HeaderHelper
import no.nav.tms.personopplysninger.api.routeConfig
import no.nav.tms.token.support.azure.exchange.AzureService
import org.junit.jupiter.api.Test

class HentRetningsnumreRouteTest : RouteTest() {

    private val kodeverkUrl = "http://kodeverk"
    private val kodeverkClientId = "cluster.namespace.kodeverk"
    private val kodeverkToken = "<kodeverk-token>"

    private val azureService: AzureService = mockk<AzureService>().also {
        coEvery { it.getAccessToken(kodeverkClientId) } returns kodeverkToken
    }

    private val internalRouteConfig: InternalRouteConfig = { client ->
        routeConfig {
            kodeverkRoutes(
                KodeverkConsumer(client, azureService, kodeverkUrl, kodeverkClientId)
            )
        }
    }

    private val hentRetningsnumrePath = "/retningsnumre"

    private val externalResponse = """
{
  "betydninger": {
    "+47": [
      {
        "gyldigFra": "1900-01-01",
        "gyldigTil": "9999-12-31",
        "beskrivelser": {
          "nb": {
            "term": "Norge",
            "tekst": "Norge"
          }
        }
      }
    ],
    "+358": [
      {
        "gyldigFra": "1900-01-01",
        "gyldigTil": "9999-12-31",
        "beskrivelser": {
          "nb": {
            "term": "Finland",
            "tekst": "Finland"
          }
        }
      }
    ]
  }
}
    """

    @Test
    fun `henter retningsnumre via kodeverk`() = apiTest(internalRouteConfig) {client ->
        externalService(kodeverkUrl) {
            get("/api/v1/kodeverk/Retningsnumre/koder/betydninger") {
                call.respondText(externalResponse, ContentType.Application.Json)
            }
        }

        val response = client.get(hentRetningsnumrePath)

        response.status shouldBe HttpStatusCode.OK
        response.json().first().let {
            it["landskode"].asText() shouldBe "+358"
            it["land"].asText() shouldBe "Finland"
        }
    }

    @Test
    fun `bruker riktige headers mot kodeverk`() = apiTest(internalRouteConfig) { client ->
        var headers: Headers? = null

        externalService(kodeverkUrl) {
            get("/api/v1/kodeverk/Retningsnumre/koder/betydninger") {
                headers = call.request.headers
                call.respondText("{}", contentType = ContentType.Application.Json)
            }
        }

        client.get(hentRetningsnumrePath)

        headers.shouldNotBeNull().let {
            it[HttpHeaders.Authorization] shouldBe "Bearer $kodeverkToken"
            it[HeaderHelper.NAV_CONSUMER_ID_HEADER] shouldBe HeaderHelper.NAV_CONSUMER_ID
            it[HeaderHelper.CALL_ID_HEADER].shouldNotBeNull()
        }
    }

    @Test
    fun `svarer med InternalServerError ved feil mot kodeverk`() = apiTest(internalRouteConfig) {client ->
        externalService(kodeverkUrl) {
            get("/api/v1/kodeverk/Retningsnumre/koder/betydninger") {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        val response = client.get(hentRetningsnumrePath)

        response.status shouldBe HttpStatusCode.InternalServerError
    }
}
