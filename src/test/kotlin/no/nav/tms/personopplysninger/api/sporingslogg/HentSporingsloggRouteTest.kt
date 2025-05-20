package no.nav.tms.personopplysninger.api.sporingslogg

import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tms.personopplysninger.api.InternalRouteConfig
import no.nav.tms.personopplysninger.api.RouteTest
import no.nav.tms.personopplysninger.api.common.TokenExchanger
import no.nav.tms.personopplysninger.api.kodeverk.KodeverkConsumer
import no.nav.tms.personopplysninger.api.routeConfig
import no.nav.tms.token.support.azure.exchange.AzureService
import org.junit.jupiter.api.Test

class HentSporingsloggRouteTest: RouteTest() {

    private val eregServicesUrl = "http://ereg-services"
    private val sporingsloggUrl = "http://sporingslogg"
    private val sporingsloggToken = "<sporingslogg-token>"
    private val kodeverkUrl = "http://kodeverk"
    private val kodeverkClientId = "kodeverk"
    private val kodeverkToken = "<kodeverk-token>"

    private val tokenExchanger = mockk<TokenExchanger>().also {
        coEvery { it.sporingsloggToken(any()) } returns sporingsloggToken
    }

    private val azureService = mockk<AzureService>().also {
        coEvery { it.getAccessToken(kodeverkClientId) } returns kodeverkToken
    }

    private val internalRouteConfig: InternalRouteConfig = { client ->
        val sporingsloggConsumer = SporingsloggConsumer(client, sporingsloggUrl, tokenExchanger)
        val eregServicesConsumer = EregServicesConsumer(client, eregServicesUrl)
        val kodeverkConsumer = KodeverkConsumer(client, azureService, kodeverkUrl, kodeverkClientId)

        routeConfig {
            sporingsloggRoutes(
                SporingsloggService(
                    sporingsloggConsumer = sporingsloggConsumer,
                    eregServicesConsumer = eregServicesConsumer,
                    kodeverkConsumer = kodeverkConsumer
                )
            )
        }
    }

    private fun ApplicationTestBuilder.setupDefaultExternalRoutes(
        setupSporingslogg: Boolean = true,
        setupEreg: Boolean = true,
        setupKodeverk: Boolean = true,
    ) {
        if (setupSporingslogg) {
            externalService(sporingsloggUrl) {
                get("/api/les") {
                    call.respondText(
                        SporingsloggTestData.ExternalResponse.sporingslogg,
                        contentType = ContentType.Application.Json
                    )
                }
            }
        }

        if (setupEreg) {
            externalService(eregServicesUrl) {
                get("/v1/organisasjon/{orgnr}/noekkelinfo") {
                    if (call.request.pathVariables["orgnr"] == SporingsloggTestData.mottaker) {
                        call.respondText(
                            SporingsloggTestData.ExternalResponse.eregServices,
                            contentType = ContentType.Application.Json
                        )
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
        }

        if (setupKodeverk) {
            externalService(kodeverkUrl) {
                get("/api/v1/kodeverk/Tema/koder/betydninger") {
                    call.respondText(
                        SporingsloggTestData.ExternalResponse.kodeverkTema,
                        contentType = ContentType.Application.Json
                    )
                }
            }
        }
    }

    private val hentSporingsloggPath = "/sporingslogg"

    @Test
    fun `henter sporingslogg fra baktjenester`() = apiTest(internalRouteConfig) {
        setupDefaultExternalRoutes()

        val response = client.get(hentSporingsloggPath)

        response.status shouldBe HttpStatusCode.OK

        response.json()[0].let { logg ->

            logg["tema"].asText() shouldBe SporingsloggTestData.tema
            logg["mottaker"].asText() shouldBe SporingsloggTestData.mottaker
            logg["mottakernavn"].asText() shouldBe SporingsloggTestData.mottakernavn
            logg["leverteData"].asText() shouldBe SporingsloggTestData.leverteData
            logg["samtykkeToken"].asText() shouldBe SporingsloggTestData.samtykkeToken
            logg["uthentingsTidspunkt"].asLocalDateTime() shouldBe SporingsloggTestData.uthentingsTidspunkt
        }

    }
}
