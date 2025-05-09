package no.nav.tms.personopplysninger.api.personalia

import com.fasterxml.jackson.databind.JsonNode
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tms.personopplysninger.api.ApiTest
import no.nav.tms.personopplysninger.api.InternalRouteConfig
import no.nav.tms.personopplysninger.api.common.HeaderHelper
import no.nav.tms.personopplysninger.api.common.TokenExchanger
import no.nav.tms.personopplysninger.api.personalia.pdl.*
import no.nav.tms.personopplysninger.api.routeConfig
import org.junit.jupiter.api.Test

class EndreTelefonnummerApiTest : ApiTest() {

    private val pdlApiUrl = "http://pdl-api"
    private val pdlMottakUrl = "http://pdl-mottak"
    private val behandlingsnummer = "123"
    private val pdlApiToken = "<api-token>"
    private val pdlMottakToken = "<mottak-token>"


    private val tokenExchanger: TokenExchanger = mockk<TokenExchanger>().also {
        coEvery { it.pdlApiToken(any()) } returns pdlApiToken
        coEvery { it.pdlMottakToken(any()) } returns pdlMottakToken
    }

    private val internalRouteConfig: InternalRouteConfig = { client ->
        val pdlApiConsumer = PdlApiConsumer(client, pdlApiUrl, behandlingsnummer, tokenExchanger)
        val pdlMottakConsumer = PdlMottakConsumer(client, pdlMottakUrl, tokenExchanger, pollCount = 1, pollIntervalMs = 50)

        routeConfig {
            personalia(
                personaliaService = mockk(),
                oppdaterPersonaliaService = OppdaterPersonaliaService(pdlApiConsumer, pdlMottakConsumer)
            )
        }
    }

    private val endreTelefonnummerPath = "/endreTelefonnummer"
    private val endringRequest = TelefonnummerEndring(landskode = "+47", nummer = "12345678")

    @Test
    fun `tillater endring av telefonnummer via pdl-mottak`() = apiTest(internalRouteConfig) { client ->

        val pollEndringPath = "/poll/endring"
        val pollEndringResponse = listOf(
            PendingEndring(
                status = PendingEndring.Status(
                    statusType = "DONE"
                )
            )
        )

        var capturedPayload: JsonNode? = null

        externalService(pdlMottakUrl) {
            post("/api/v1/endringer") {
                capturedPayload = call.receiveJson()
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
            setBody(endringRequest)
        }

        response.status shouldBe HttpStatusCode.OK
        response.json().let {
            it["statusType"].asText() shouldBe "OK"
            it["error"].isNull shouldBe true
        }

        val endringPayload = capturedPayload.shouldNotBeNull()

        endringPayload["personopplysninger"]
        .first()
        .let {
            it["ident"].asText() shouldBe testIdent
            it["opplysningstype"].asText() shouldBe "TELEFONNUMMER"
            it["endringstype"].asText() shouldBe EndringsType.OPPRETT.name

            val melding = it["endringsmelding"]

            melding["nummer"].asText() shouldBe endringRequest.nummer
            melding["landskode"].asText() shouldBe endringRequest.landskode
            melding["@type"].asText() shouldBe "TELEFONNUMMER"
            melding["kilde"].asText() shouldBe "BRUKER SELV"
        }
    }

    @Test
    fun `bruker riktige headers mot pdl-mottak`() = apiTest(internalRouteConfig) { client ->

        var headers: Headers? = null

        externalService(pdlMottakUrl) {
            post("/api/v1/endringer") {
                headers = call.request.headers

                call.respond(HttpStatusCode.OK)
            }
        }

        client.post {
            url(endreTelefonnummerPath)
            contentType(ContentType.Application.Json)
            setBody(endringRequest)
        }

        headers.shouldNotBeNull().let {
            it[HttpHeaders.Authorization] shouldBe "Bearer $pdlMottakToken"
            it[HeaderHelper.CALL_ID_HEADER].shouldNotBeNull()
            it[HeaderHelper.NAV_CONSUMER_ID_HEADER] shouldBe HeaderHelper.NAV_CONSUMER_ID
        }
    }

    @Test
    fun `svarer med InternalServerError ved feil mot pdl-mottak`() = apiTest(internalRouteConfig) { client ->

        externalService(pdlMottakUrl) {
            post("/api/v1/endringer") {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        val response = client.post {
            url(endreTelefonnummerPath)
            contentType(ContentType.Application.Json)
            setBody(endringRequest)
        }

        response.status shouldBe HttpStatusCode.InternalServerError
    }

    @Test
    fun `videreformidler feilmelding fra pdl-mottak`() = apiTest(internalRouteConfig) { client ->

        val pollEndringPath = "/poll/endring"
        val pollEndringResponse = listOf(
            PendingEndring(
                status = PendingEndring.Status(
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
            )
        )

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
            setBody(endringRequest)
        }

        response.status shouldBe HttpStatusCode.OK
        response.json().let {
            it["statusType"].asText() shouldBe "ERROR"
            it["error"].isNull shouldBe false

            it["error"]["message"].asText() shouldBe "Person ikke funnet i TPS"
        }
    }

    @Test
    fun `svarer ok dersom polling mot pdl-mottak og status enda er PENDING`() = apiTest(internalRouteConfig) { client ->
        val pollEndringPath = "/poll/endring"
        val pollEndringResponse = listOf(
            PendingEndring(
                status = PendingEndring.Status(
                    statusType = "PENDING"
                )
            )
        )

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
            setBody(endringRequest)
        }

        response.status shouldBe HttpStatusCode.OK
        response.json().let {
            it["statusType"].asText() shouldBe "OK"
            it["error"].isNull shouldBe true
        }
    }
}
