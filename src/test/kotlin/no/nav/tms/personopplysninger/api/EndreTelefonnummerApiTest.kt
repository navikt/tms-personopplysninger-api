package no.nav.tms.personopplysninger.api

import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tms.personopplysninger.api.common.TokenExchanger
import no.nav.tms.personopplysninger.api.personalia.*
import no.nav.tms.personopplysninger.api.personalia.pdl.PdlApiConsumer
import no.nav.tms.personopplysninger.api.personalia.pdl.PdlMottakConsumer
import org.junit.jupiter.api.Test

class EndreTelefonnummerApiTest : ApiTest() {

    private val hentPersonaliaService: HentPersonaliaService = mockk()

    private val tokenExchanger: TokenExchanger = mockk<TokenExchanger>().also {
        coEvery { it.pdlApiToken(any()) } returns "<api-token>"
        coEvery { it.pdlMottakToken(any()) } returns "<mottak-token>"
    }

    private val pdlApiUrl = "http://pdl-api"
    private val pdlMottakUrl = "http://pdl-mottak"
    private val behandlingsnummer = "123"

    private val internalRouteConfig: InternalRouteConfig = { client ->
        val pdlApiConsumer = PdlApiConsumer(client, pdlApiUrl, behandlingsnummer, tokenExchanger)
        val pdlMottakConsumer = PdlMottakConsumer(client, pdlMottakUrl, tokenExchanger)

        val route: Route.() -> Unit = {
            personalia(hentPersonaliaService, OppdaterPersonaliaService(pdlApiConsumer, pdlMottakConsumer))
        }

        route
    }

    private val endreTelefonnummerPath = "/endreTelefonnummer"
    private val endring = TelefonnummerEndring(landskode = "dummy", nummer = "dummy")

    @Test
    fun `tillater endring av telefonnummer`() = setupApi(
        internalRouteConfig = internalRouteConfig
    ).runTest { client ->

        val pollEndringPath = "/poll/ending"
        val pollEndringResponse = listOf(PendingEndring(
            status = PendingEndring.Status(
                endringId = 123,
                statusType = "DONE"
            )
        ))

        externalService(pdlMottakUrl) {
            post("/api/v1/endringer") {
                call.response.header(HttpHeaders.Location, pollEndringPath)
                call.respond(HttpStatusCode.OK)
            }

            get(pollEndringPath) {
                call.respond(pollEndringResponse)
            }
        }

        val response = client.post {
            url(endreTelefonnummerPath)
            contentType(ContentType.Application.Json)
            setBody(endring)
        }

        response.status shouldBe HttpStatusCode.OK
        response.json().let {
            it["statusType"].asText() shouldBe "OK"
            it["error"].isNull shouldBe true
        }
    }

    @Test
    fun `svarer med feil ved feil mot pdl-mottak`() = setupApi(
        internalRouteConfig = internalRouteConfig
    ).runTest { client ->

        externalService(pdlMottakUrl) {
            post("/api/v1/endringer") {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        val response = client.post {
            url(endreTelefonnummerPath)
            contentType(ContentType.Application.Json)
            setBody(endring)
        }

        response.status shouldBe HttpStatusCode.InternalServerError
    }

    @Test
    fun `videreformidler feilmelding fra pdl-mottak`() = setupApi(
        internalRouteConfig = internalRouteConfig
    ).runTest { client ->

        val pollEndringPath = "/poll/ending"
        val pollEndringResponse = listOf(PendingEndring(
            status = PendingEndring.Status(
                endringId = 123,
                statusType = "VALIDERINGSFEIL",
                substatus = listOf(
                    PendingEndring.Substatus(
                        beskrivelse = "Person ikke funnet i TPS",
                        domene = "TPS",
                        kode = "VALIDERINGSFEIL",
                        referanse = "b8a2619b-1e9e-4d67-9d16-3ec678791685",
                        status = "ERROR"
                    )
                )
            )
        ))

        externalService(pdlMottakUrl) {
            post("/api/v1/endringer") {
                call.response.header(HttpHeaders.Location, pollEndringPath)
                call.respond(HttpStatusCode.OK)
            }

            get(pollEndringPath) {
                call.respond(pollEndringResponse)
            }
        }

        val response = client.post {
            url(endreTelefonnummerPath)
            contentType(ContentType.Application.Json)
            setBody(endring)
        }

        response.status shouldBe HttpStatusCode.OK
        response.json().let {
            it["statusType"].asText() shouldBe "ERROR"
            it["error"].isNull shouldBe false

            it["error"]["message"].asText() shouldBe "Person ikke funnet i TPS"
        }
    }
}
