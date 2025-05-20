package no.nav.tms.personopplysninger.api.medl

import com.fasterxml.jackson.databind.JsonNode
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

class HentMedlRouteTest : RouteTest() {

    private val medlApiUrl = "http://medl-api"
    private val medlToken = "<medl-token>"

    private val kodeverkUrl = "http://kodeverk"
    private val kodeverkClientId = "cluster.namespace.kodeverk"
    private val kodeverkToken = "<kodeverk-token>"

    private val tokenExchanger: TokenExchanger = mockk<TokenExchanger>().also {
        coEvery { it.medlToken(any()) } returns medlToken
    }

    private val azureService: AzureService = mockk<AzureService>().also {
        coEvery { it.getAccessToken(kodeverkClientId) } returns kodeverkToken
    }

    private val internalRouteConfig: InternalRouteConfig = { client ->
        routeConfig {
            medlRoutes(
                MedlService(
                    MedlConsumer(client, medlApiUrl, tokenExchanger),
                    KodeverkConsumer(client, azureService, kodeverkUrl, kodeverkClientId)
                )
            )
        }
    }

    private val hentKontaktinformasjonPath = "/medl"

    private val medlApiResponse = """
{
  "perioder": [
    {
      "fraOgMed": "2020-01-01",
      "hjemmel": "FO_1408_14_1_a",
      "kilde": "AVGSYS",
      "lovvalgsland": "NOR",
      "medlem": false,
      "tilOgMed": "2020-02-01",
      "trygdedekning": "Unntatt"
    },
    {
      "fraOgMed": "2020-02-01",
      "hjemmel": "FO_1408_14_1_a",
      "kilde": "LAANEKASSEN",
      "lovvalgsland": "NOR",
      "medlem": false,
      "studieinformasjon": {
        "statsborgerland": "SWE",
        "studieland": "SWE"
      },
      "tilOgMed": "2020-03-01",
      "trygdedekning": "Unntatt"
    }
  ]
}
""".trimIndent()

    private val kodeverkDekningResponse = """
{
  "betydninger": {
    "Unntatt": [
      {
        "gyldigFra": "1900-01-01",
        "gyldigTil": "9999-12-31",
        "beskrivelser": {
          "nb": { 
            "term": "Unntatt",
            "tekst": "Ikke medlem - eller unntatt fra medlemskap"
          }
        }
      }
    ]
  }
}
""".trimIndent()

    private val kodeverkGrunnlagResponse = """
{
  "betydninger": {
    "FO_1408_14_1_a": [
      {
        "gyldigFra": "1900-01-01",
        "gyldigTil": "9999-12-31",
        "beskrivelser": {
          "nb": {
            "term": "EØS 1408/71 - 14.1.a",
            "tekst": "EØS-forordning 1408/1971"
          }
        }
      }
    ]
  }
}
""".trimIndent()

    private val kodeverkLandkoderResponse = """
{
  "betydninger": {
    "NOR": [
      {
        "gyldigFra": "1900-01-01",
        "gyldigTil": "9999-12-31",
        "beskrivelser": {
          "nb": {
            "term": "NORGE"
          }
        }
      }
    ],
    "SWE": [
      {
        "gyldigFra": "1900-01-01",
        "gyldigTil": "9999-12-31",
        "beskrivelser": {
          "nb": {
            "term": "SVERIGE"
          }
        }
      }
    ]
  }
}
""".trimIndent()

    @Test
    fun `henter unntak for medlemskap i norsk folketrygd via medl-api`() = apiTest(internalRouteConfig) {client ->
        externalService(kodeverkUrl) {
            get("/api/v1/kodeverk/DekningMedl/koder/betydninger") {
                call.respondText(kodeverkDekningResponse, ContentType.Application.Json)
            }

            get("/api/v1/kodeverk/GrunnlagMedl/koder/betydninger") {
                call.respondText(kodeverkGrunnlagResponse, ContentType.Application.Json)
            }

            get("/api/v1/kodeverk/Landkoder/koder/betydninger") {
                call.respondText(kodeverkLandkoderResponse, ContentType.Application.Json)
            }
        }

        var capturedPayload: JsonNode? = null

        externalService(medlApiUrl) {
            post("/rest/v1/innsyn") {
                capturedPayload = call.receiveJson()
                call.respondText(medlApiResponse, ContentType.Application.Json)
            }
        }

        val response = client.get(hentKontaktinformasjonPath)

        response.status shouldBe HttpStatusCode.OK

        response.json()["perioder"][0].let {
            it["fraOgMed"].asText() shouldBe "2020-01-01"
            it["tilOgMed"].asText() shouldBe "2020-02-01"
            it["medlem"].asBoolean() shouldBe false
            it["hjemmel"].asText() shouldBe "EØS-forordning 1408/1971"
            it["trygdedekning"].asText() shouldBe "Ikke medlem - eller unntatt fra medlemskap"
            it["lovvalgsland"].asText() shouldBe "NORGE"
            it["kilde"].asText() shouldBe "AVGSYS"
        }

        response.json()["perioder"][1].let {
            it["fraOgMed"].asText() shouldBe "2020-02-01"
            it["tilOgMed"].asText() shouldBe "2020-03-01"
            it["medlem"].asBoolean() shouldBe false
            it["hjemmel"].asText() shouldBe "EØS-forordning 1408/1971"
            it["trygdedekning"].asText() shouldBe "Ikke medlem - eller unntatt fra medlemskap"
            it["lovvalgsland"].asText() shouldBe "NORGE"
            it["kilde"].asText() shouldBe "LAANEKASSEN"
            it["studieinformasjon"]["statsborgerland"].asText() shouldBe "SVERIGE"
            it["studieinformasjon"]["studieland"].asText() shouldBe "SVERIGE"
        }

        capturedPayload.shouldNotBeNull().let {
            it["personident"].asText() shouldBe testIdent
        }
    }

    @Test
    fun `bruker riktige headers mot medl-api`() = apiTest(internalRouteConfig) { client ->
        var headers: Headers? = null

        externalService(kodeverkUrl) {
            get("/api/v1/kodeverk/DekningMedl/koder/betydninger") {
                call.respondText(kodeverkDekningResponse, ContentType.Application.Json)
            }

            get("/api/v1/kodeverk/GrunnlagMedl/koder/betydninger") {
                call.respondText(kodeverkGrunnlagResponse, ContentType.Application.Json)
            }

            get("/api/v1/kodeverk/Landkoder/koder/betydninger") {
                call.respondText(kodeverkLandkoderResponse, ContentType.Application.Json)
            }
        }

        externalService(medlApiUrl) {
            post("/rest/v1/innsyn") {
                headers = call.request.headers
                call.respondText(medlApiResponse, ContentType.Application.Json)
            }
        }

        client.get(hentKontaktinformasjonPath)

        headers.shouldNotBeNull().let {
            it[HttpHeaders.Authorization] shouldBe "Bearer $medlToken"
            it[HeaderHelper.NAV_CONSUMER_ID_HEADER] shouldBe HeaderHelper.NAV_CONSUMER_ID
            it[HeaderHelper.CALL_ID_HEADER].shouldNotBeNull()
        }
    }

    @Test
    fun `svarer med InternalServerError ved feil mot medl-api`() = apiTest(internalRouteConfig) {client ->
        externalService(kodeverkUrl) {
            get("/api/v1/kodeverk/DekningMedl/koder/betydninger") {
                call.respondText(kodeverkDekningResponse, ContentType.Application.Json)
            }

            get("/api/v1/kodeverk/GrunnlagMedl/koder/betydninger") {
                call.respondText(kodeverkGrunnlagResponse, ContentType.Application.Json)
            }

            get("/api/v1/kodeverk/Landkoder/koder/betydninger") {
                call.respondText(kodeverkLandkoderResponse, ContentType.Application.Json)
            }
        }

        externalService(medlApiUrl) {
            post("/rest/v1/innsyn") {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        val response = client.get(hentKontaktinformasjonPath)

        response.status shouldBe HttpStatusCode.InternalServerError
    }
}
