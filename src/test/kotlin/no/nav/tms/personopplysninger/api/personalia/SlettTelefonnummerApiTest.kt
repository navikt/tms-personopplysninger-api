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
import no.nav.tms.personopplysninger.api.personalia.pdl.EndringsType
import no.nav.tms.personopplysninger.api.personalia.pdl.PdlApiConsumer
import no.nav.tms.personopplysninger.api.personalia.pdl.PdlMottakConsumer
import no.nav.tms.personopplysninger.api.personalia.pdl.PendingEndring
import org.junit.jupiter.api.Test

class SlettTelefonnummerApiTest : ApiTest() {

    private val pdlApiUrl = "http://pdl-api"
    private val pdlMottakUrl = "http://pdl-mottak"
    private val behandlingsnummer = "B123"
    private val pdlApiToken = "<api-token>"
    private val pdlMottakToken = "<mottak-token>"

    private val tokenExchanger: TokenExchanger = mockk<TokenExchanger>().also {
        coEvery { it.pdlApiToken(any()) } returns pdlApiToken
        coEvery { it.pdlMottakToken(any()) } returns pdlMottakToken
    }

    private val internalRouteConfig: InternalRouteConfig = { client ->
        val pdlApiConsumer = PdlApiConsumer(client, pdlApiUrl, behandlingsnummer, tokenExchanger)
        val pdlMottakConsumer =
            PdlMottakConsumer(client, pdlMottakUrl, tokenExchanger, pollCount = 1, pollIntervalMs = 50)

        val route: Route.() -> Unit = {
            personalia(
                personaliaService = mockk(),
                oppdaterPersonaliaService = OppdaterPersonaliaService(pdlApiConsumer, pdlMottakConsumer)
            )
        }

        route
    }

    private val slettTelefonnummerPath = "/slettTelefonnummer"
    private val slettRequest = TelefonnummerEndring(landskode = "+47", nummer = "12345678")

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

        val opplysningsId = "123"

        var capturedPayload: JsonNode? = null

        externalService(pdlApiUrl) {
            post("/graphql") {
                call.respond(PdlResponseBuilder.hentTelefonnummerResponse(
                    landskode = slettRequest.landskode,
                    nummer = slettRequest.nummer,
                    opplysningsId = opplysningsId
                ))
            }
        }

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
            url(slettTelefonnummerPath)
            contentType(ContentType.Application.Json)
            setBody(slettRequest)
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
                it["endringstype"].asText() shouldBe EndringsType.OPPHOER.name
                it["opplysningsId"].asText() shouldBe opplysningsId

                val melding = it["endringsmelding"]
                melding["@type"].asText() shouldBe "OPPHOER"
            }
    }

    @Test
    fun `bruker riktig headers`() = apiTest(internalRouteConfig) { client ->

        var apiHeaders: Headers? = null
        var mottakHeaders: Headers? = null

        externalService(pdlApiUrl) {
            post("/graphql") {
                apiHeaders = call.request.headers

                call.respond(PdlResponseBuilder.hentTelefonnummerResponse(
                    landskode = slettRequest.landskode,
                    nummer = slettRequest.nummer,
                    opplysningsId = "123"
                ))
            }
        }

        externalService(pdlMottakUrl) {
            post("/api/v1/endringer") {
                mottakHeaders = call.request.headers

                call.respond(HttpStatusCode.OK)
            }
        }

        client.post {
            url(slettTelefonnummerPath)
            contentType(ContentType.Application.Json)
            setBody(slettRequest)
        }

        apiHeaders.shouldNotBeNull().let {
            it[HttpHeaders.Authorization] shouldBe "Bearer $pdlApiToken"
            it[HeaderHelper.CALL_ID_HEADER].shouldNotBeNull()
            it[HeaderHelper.NAV_CONSUMER_ID_HEADER] shouldBe HeaderHelper.NAV_CONSUMER_ID
            it["Behandlingsnummer"] shouldBe behandlingsnummer
        }

        mottakHeaders.shouldNotBeNull().let {
            it[HttpHeaders.Authorization] shouldBe "Bearer $pdlMottakToken"
            it[HeaderHelper.CALL_ID_HEADER].shouldNotBeNull()
            it[HeaderHelper.NAV_CONSUMER_ID_HEADER] shouldBe HeaderHelper.NAV_CONSUMER_ID
        }
    }

    @Test
    fun `svarer med feil ved feil mot pdl-api`() = apiTest(internalRouteConfig) { client ->

        externalService(pdlApiUrl) {
            post("/graphql") {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        val response = client.post {
            url(slettTelefonnummerPath)
            contentType(ContentType.Application.Json)
            setBody(slettRequest)
        }

        response.status shouldBe HttpStatusCode.InternalServerError
    }

    @Test
    fun `svarer med feil ved feil mot pdl-mottak`() = apiTest(internalRouteConfig) { client ->

        externalService(pdlApiUrl) {
            post("/graphql") {
                call.respond(PdlResponseBuilder.hentTelefonnummerResponse(
                    landskode = slettRequest.landskode,
                    nummer = slettRequest.nummer,
                    opplysningsId = "123"
                ))
            }
        }

        externalService(pdlMottakUrl) {
            post("/api/v1/endringer") {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        val response = client.post {
            url(slettTelefonnummerPath)
            contentType(ContentType.Application.Json)
            setBody(slettRequest)
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
        externalService(pdlApiUrl) {
            post("/graphql") {
                call.respond(PdlResponseBuilder.hentTelefonnummerResponse(
                    landskode = slettRequest.landskode,
                    nummer = slettRequest.nummer,
                    opplysningsId = "123"
                ))
            }
        }


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
            url(slettTelefonnummerPath)
            contentType(ContentType.Application.Json)
            setBody(slettRequest)
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

        externalService(pdlApiUrl) {
            post("/graphql") {
                call.respond(PdlResponseBuilder.hentTelefonnummerResponse(
                    landskode = slettRequest.landskode,
                    nummer = slettRequest.nummer,
                    opplysningsId = "123"
                ))
            }
        }

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
            url(slettTelefonnummerPath)
            contentType(ContentType.Application.Json)
            setBody(slettRequest)
        }

        response.status shouldBe HttpStatusCode.OK
        response.json().let {
            it["statusType"].asText() shouldBe "OK"
            it["error"].isNull shouldBe true
        }
    }
}
