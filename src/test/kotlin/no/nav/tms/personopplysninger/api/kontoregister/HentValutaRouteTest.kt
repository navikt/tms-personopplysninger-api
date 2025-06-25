package no.nav.tms.personopplysninger.api.kontoregister

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.mockk.mockk
import no.nav.tms.personopplysninger.api.RouteTest
import no.nav.tms.personopplysninger.api.InternalRouteConfig
import no.nav.tms.personopplysninger.api.common.HeaderHelper
import no.nav.tms.personopplysninger.api.common.TokenExchanger
import no.nav.tms.personopplysninger.api.routeConfig
import org.junit.jupiter.api.Test

class HentValutaRouteTest : RouteTest() {

    private val kontoRegisterUrl = "http://kontoregister"

    private val tokenExchanger: TokenExchanger = mockk<TokenExchanger>()

    private val internalRouteConfig: InternalRouteConfig = { client ->
        routeConfig {
            kontoregisterRoutes(
                KontoregisterConsumer(client, kontoRegisterUrl, tokenExchanger)
            )
        }
    }

    private val hentValutaPath = "/valuta"

    private val externalResponse = """
[ 
    {
      "valutakode" : "EUR",
      "valuta" : "Euro"
    }, {
      "valutakode" : "NOK",
      "valuta" : "Norsk krone"
    }
]
    """

    @Test
    fun `henter valutakoder via kontoregister`() = apiTest(internalRouteConfig) {client ->

        externalService(kontoRegisterUrl) {
            get("/api/system/v1/hent-valutakoder") {
                call.respondText(externalResponse, ContentType.Application.Json)
            }
        }

        val response = client.get(hentValutaPath)

        response.status shouldBe HttpStatusCode.OK
        response.json().first().let {
            it["valutakode"].asText() shouldBe "EUR"
            it["kode"].asText() shouldBe "EUR"
            it["valuta"].asText() shouldBe "Euro"
            it["tekst"].asText() shouldBe "Euro"
        }
    }

    @Test
    fun `bruker ingen auth- eller nav-headers mot kontoregister`() = apiTest(internalRouteConfig) { client ->
        var headers: Headers? = null

        externalService(kontoRegisterUrl) {
            get("/api/system/v1/hent-valutakoder") {
                headers = call.request.headers
                call.respond(emptyList<KontoResponse.Landkode>())
            }
        }

        client.get(hentValutaPath)

        headers.shouldNotBeNull().let {
            it[HttpHeaders.Authorization].shouldBeNull()
            it[HeaderHelper.CALL_ID_HEADER].shouldBeNull()
            it[HeaderHelper.NAV_CONSUMER_ID_HEADER].shouldBeNull()
        }
    }

    @Test
    fun `svarer med InternalServerError ved feil mot kontoregister`() = apiTest(internalRouteConfig) {client ->
        externalService(kontoRegisterUrl) {
            get("/api/system/v1/hent-valutakoder") {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        val response = client.get(hentValutaPath)

        response.status shouldBe HttpStatusCode.InternalServerError
    }
}
