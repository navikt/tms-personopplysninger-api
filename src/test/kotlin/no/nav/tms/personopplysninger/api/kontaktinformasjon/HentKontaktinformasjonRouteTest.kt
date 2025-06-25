package no.nav.tms.personopplysninger.api.kontaktinformasjon

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
import no.nav.tms.personopplysninger.api.common.TokenExchanger
import no.nav.tms.personopplysninger.api.kodeverk.KodeverkConsumer
import no.nav.tms.personopplysninger.api.routeConfig
import no.nav.tms.token.support.azure.exchange.AzureService
import org.junit.jupiter.api.Test

class HentKontaktinformasjonRouteTest : RouteTest() {

    private val krrProxyUrl = "http://krr-proxy"
    private val krrProxyToken = "<krr-token>"

    private val kodeverkUrl = "http://kodeverk"
    private val kodeverkClientId = "cluster.namespace.kodeverk"
    private val kodeverkToken = "<kodeverk-token>"

    private val tokenExchanger: TokenExchanger = mockk<TokenExchanger>().also {
        coEvery { it.krrProxyToken(any()) } returns krrProxyToken
    }

    private val azureService: AzureService = mockk<AzureService>().also {
        coEvery { it.getAccessToken(kodeverkClientId) } returns kodeverkToken
    }

    private val internalRouteConfig: InternalRouteConfig = { client ->
        routeConfig {
            kontaktinformasjonRoutes(
                KontaktinformasjonService(
                    KontaktinformasjonConsumer(client, krrProxyUrl, tokenExchanger),
                    KodeverkConsumer(client, azureService, kodeverkUrl, kodeverkClientId)
                )
            )
        }
    }

    private val hentKontaktinformasjonPath = "/kontaktinformasjon"

    private val krrResponse = """
{
  "personer": {
    "$testIdent": {
      "personident": "$testIdent",
      "aktiv": true,
      "kanVarsles": false,
      "reservert": false,
      "epostadresse": "test@dummy.com",
      "mobiltelefonnummer": "12345678",
      "spraak": "nb"
    }
  }
}
""".trimIndent()

    private val kodeverkResponse = """
{
  "betydninger": {
    "NB": [
      {
        "gyldigFra": "2017-10-27",
        "gyldigTil": "9999-12-31",
        "beskrivelser": {
          "nb": {
            "term": "Norsk",
            "tekst": "Norsk"
          }
        }
      }
    ]
  }
}
""".trimIndent()

    @Test
    fun `henter kontaktinformasjon via krr-proxy`() = apiTest(internalRouteConfig) {client ->
        externalService(kodeverkUrl) {
            get("/api/v1/kodeverk/Språk/koder/betydninger") {
                call.respondText(kodeverkResponse, ContentType.Application.Json)
            }
        }

        externalService(krrProxyUrl) {
            post("/rest/v1/personer") {
                call.respondText(krrResponse, ContentType.Application.Json)
            }
        }

        val response = client.get(hentKontaktinformasjonPath)

        response.status shouldBe HttpStatusCode.OK
        response.json().let {
            it["epostadresse"].asText() shouldBe "test@dummy.com"
            it["mobiltelefonnummer"].asText() shouldBe "12345678"
            it["reservert"].asBoolean() shouldBe false
            it["spraak"].asText() shouldBe "Bokmål"
        }
    }

    @Test
    fun `bruker riktige headers mot krr-proxy`() = apiTest(internalRouteConfig) { client ->
        var headers: Headers? = null

        externalService(kodeverkUrl) {
            get("/api/v1/kodeverk/Språk/koder/betydninger") {
                call.respondText(kodeverkResponse, ContentType.Application.Json)
            }
        }

        externalService(krrProxyUrl) {
            post("/rest/v1/personer") {
                headers = call.request.headers
                call.respondText(krrResponse, ContentType.Application.Json)
            }
        }

        client.get(hentKontaktinformasjonPath)

        headers.shouldNotBeNull().let {
            it[HttpHeaders.Authorization] shouldBe "Bearer $krrProxyToken"
            it[HeaderHelper.NAV_CONSUMER_ID_HEADER] shouldBe HeaderHelper.NAV_CONSUMER_ID
            it[HeaderHelper.CALL_ID_HEADER].shouldNotBeNull()
        }
    }

    @Test
    fun `svarer med InternalServerError ved feil mot krr-proxy`() = apiTest(internalRouteConfig) {client ->
        externalService(kodeverkUrl) {
            get("/api/v1/kodeverk/Språk/koder/betydninger") {
                call.respondText(kodeverkResponse, ContentType.Application.Json)
            }
        }

        externalService(krrProxyUrl) {
            post("/rest/v1/personer") {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        val response = client.get(hentKontaktinformasjonPath)

        response.status shouldBe HttpStatusCode.InternalServerError
    }
}
