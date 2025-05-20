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

class HentLandRouteTest : RouteTest() {

    private val kontoRegisterUrl = "http://kontoregister"

    private val tokenExchanger: TokenExchanger = mockk<TokenExchanger>()

    private val internalRouteConfig: InternalRouteConfig = { client ->
        routeConfig {
            kontoregisterRoutes(
                KontoregisterConsumer(client, kontoRegisterUrl, tokenExchanger)
            )
        }
    }

    private val hentLandPath = "/land"

    private val externalResponse = """
[
    {
      "landkode" : "NO",
      "land" : "Norge",
      "kreverIban" : true,
      "ibanLengde" : 15,
      "kreverBankkode" : false
    }
]
    """

    @Test
    fun `henter landkoder via kontoregister`() = apiTest(internalRouteConfig) {client ->
        externalService(kontoRegisterUrl) {
            get("/api/system/v1/hent-landkoder") {
                call.respondText(externalResponse, ContentType.Application.Json)
            }
        }

        val response = client.get(hentLandPath)

        response.status shouldBe HttpStatusCode.OK
        response.json().first().let {
            it["landkode"].asText() shouldBe "NO"
            it["land"].asText() shouldBe "Norge"
            it["kreverIban"].asBoolean() shouldBe true
            it["ibanLengde"].asInt() shouldBe 15
            it["kreverBankkode"].asBoolean() shouldBe false
            it["bankkodeLengde"].asTextOrNull() shouldBe null
            it["alternativLandkode"].asTextOrNull() shouldBe null
        }
    }

    @Test
    fun `bruker ingen auth- eller nav-headers mot kontoregister`() = apiTest(internalRouteConfig) { client ->
        var headers: Headers? = null

        externalService(kontoRegisterUrl) {
            get("/api/system/v1/hent-landkoder") {
                headers = call.request.headers
                call.respond(emptyList<KontoResponse.Landkode>())
            }
        }

        client.get(hentLandPath)

        headers.shouldNotBeNull().let {
            it[HttpHeaders.Authorization].shouldBeNull()
            it[HeaderHelper.CALL_ID_HEADER].shouldBeNull()
            it[HeaderHelper.NAV_CONSUMER_ID_HEADER].shouldBeNull()
        }
    }

    @Test
    fun `svarer med InternalServerError ved feil mot kontoregister`() = apiTest(internalRouteConfig) {client ->
        externalService(kontoRegisterUrl) {
            get("/api/system/v1/hent-landkoder") {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        val response = client.get(hentLandPath)

        response.status shouldBe HttpStatusCode.InternalServerError
    }
}
