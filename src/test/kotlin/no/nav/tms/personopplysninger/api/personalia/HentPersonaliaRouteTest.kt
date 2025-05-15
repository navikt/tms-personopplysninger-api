package no.nav.tms.personopplysninger.api.personalia

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tms.personopplysninger.api.RouteTest
import no.nav.tms.personopplysninger.api.InternalRouteConfig
import no.nav.tms.personopplysninger.api.common.HeaderHelper
import no.nav.tms.personopplysninger.api.common.TokenExchanger
import no.nav.tms.personopplysninger.api.kodeverk.KodeverkConsumer
import no.nav.tms.personopplysninger.api.kontoregister.KontoregisterConsumer
import no.nav.tms.personopplysninger.api.personalia.HentPersonaliaTestData.ExternalResponse
import no.nav.tms.personopplysninger.api.personalia.norg2.Norg2Consumer
import no.nav.tms.personopplysninger.api.personalia.pdl.PdlApiConsumer
import no.nav.tms.personopplysninger.api.routeConfig
import no.nav.tms.token.support.azure.exchange.AzureService
import org.junit.jupiter.api.Test

class HentPersonaliaRouteTest : RouteTest() {

    private val pdlApiUrl = "http://pdl-api"
    private val norg2Url = "http://norg2"
    private val kodeverkUrl = "http://kodeverk"
    private val kontoregisterUrl = "http://kontoregister"
    private val behandlingsnummer = "B123"
    private val pdlApiToken = "<api-token>"
    private val kodeverkClientId = "kodeverk"
    private val kodeverkToken = "<kodeverk-token>"
    private val kontoregisterToken = "<kontoregister-token>"

    private val tokenExchanger = mockk<TokenExchanger>().also {
        coEvery { it.pdlApiToken(any()) } returns pdlApiToken
        coEvery { it.kontoregisterToken(any()) } returns kontoregisterToken
    }

    private val azureService = mockk<AzureService>().also {
        coEvery { it.getAccessToken(kodeverkClientId) } returns kodeverkToken
    }

    private val internalRouteConfig: InternalRouteConfig = { client ->
        val pdlApiConsumer = PdlApiConsumer(client, pdlApiUrl, behandlingsnummer, tokenExchanger)
        val norg2Consumer = Norg2Consumer(client, norg2Url)
        val kodeverkConsumer = KodeverkConsumer(client, azureService, kodeverkUrl, kodeverkClientId)
        val kontoregisterConsumer = KontoregisterConsumer(client, kontoregisterUrl, tokenExchanger)

        routeConfig {
            personalia(
                personaliaService = HentPersonaliaService(
                    pdlApiConsumer = pdlApiConsumer,
                    norg2Consumer = norg2Consumer,
                    kodeverkConsumer = kodeverkConsumer,
                    kontoregisterConsumer = kontoregisterConsumer
                ),
                oppdaterPersonaliaService = mockk()
            )
        }
    }

    private fun ApplicationTestBuilder.setupDefaultExternalRoutes(
        setupPdl: Boolean = true,
        setupNorg2: Boolean = true,
        setupKodeverk: Boolean = true,
        setupKontoregister: Boolean = true
    ) {
        if (setupPdl) {
            externalService(pdlApiUrl) {
                post("/graphql") {
                    call.respondText(ExternalResponse.pdlHentPerson, contentType = ContentType.Application.Json)
                }
            }
        }

        if (setupNorg2) {
            externalService(norg2Url) {
                get("/api/v1/enhet/navkontor/{geografiskId}") {
                    if (call.pathParameters["geografiskId"] == HentPersonaliaTestData.geografiskTilknytning) {
                        call.respondText(ExternalResponse.norg2Enhet, contentType = ContentType.Application.Json)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                get("/api/v2/enhet/{enhetNr}/kontaktinformasjon") {
                    if (call.pathParameters["enhetNr"] == HentPersonaliaTestData.enhetsnummer) {
                        call.respondText(ExternalResponse.norg2Kontaktinfo, contentType = ContentType.Application.Json)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
        }

        if (setupKontoregister) {
            externalService(kontoregisterUrl) {
                post("/api/borger/v1/hent-aktiv-konto") {
                    call.respondText(ExternalResponse.kontoregisterKonto, contentType = ContentType.Application.Json)
                }

                get("/api/system/v1/hent-landkoder") {
                    call.respondText(ExternalResponse.kontoregisterLandkoder, contentType = ContentType.Application.Json)
                }

                get("/api/system/v1/hent-valutakoder") {
                    call.respondText(ExternalResponse.kontoregisterValutakoder, contentType = ContentType.Application.Json)
                }
            }
        }

        if (setupKodeverk) {
            externalService(kodeverkUrl) {
                get("/api/v1/kodeverk/Landkoder/koder/betydninger") {
                    call.respondText(ExternalResponse.kodeverkLandkoder, contentType = ContentType.Application.Json)
                }

                get("/api/v1/kodeverk/Kommuner/koder/betydninger") {
                    call.respondText(ExternalResponse.kodeverkKommuner, contentType = ContentType.Application.Json)
                }

                get("/api/v1/kodeverk/StatsborgerskapFreg/koder/betydninger") {
                    call.respondText(ExternalResponse.kodeverkStatsborgerskap, contentType = ContentType.Application.Json)
                }

                get("/api/v1/kodeverk/Postnummer/koder/betydninger") {
                    call.respondText(ExternalResponse.kodeverkPostnummer, contentType = ContentType.Application.Json)
                }
            }
        }
    }

    private val hentPersonaliaPath = "/personalia"

    @Test
    fun `henter personalia fra baktjenester`() = apiTest(internalRouteConfig) { client ->

        setupDefaultExternalRoutes()

        val response = client.get(hentPersonaliaPath)


        response.status shouldBe HttpStatusCode.OK
        val responseJson = response.json()

        responseJson["personalia"].let { personalia ->

            personalia["fornavn"].asText() shouldBe HentPersonaliaTestData.fornavn
            personalia["etternavn"].asText() shouldBe HentPersonaliaTestData.etternavn

            personalia["personident"]["verdi"].asText() shouldBe HentPersonaliaTestData.ident
            personalia["personident"]["type"].asText() shouldBe HentPersonaliaTestData.identtype

            personalia["kontoregisterStatus"].asText() shouldBe HentPersonaliaTestData.kontoregisterStatus
            personalia["kontonr"].asTextOrNull() shouldBe null
            personalia["utenlandskbank"]["adresse1"].asText() shouldBe HentPersonaliaTestData.bankAdresse1
            personalia["utenlandskbank"]["adresse2"].asText() shouldBe HentPersonaliaTestData.bankAdresse2
            personalia["utenlandskbank"]["adresse3"].asText() shouldBe HentPersonaliaTestData.bankAdresse3
            personalia["utenlandskbank"]["bankkode"].asText() shouldBe HentPersonaliaTestData.bankkode
            personalia["utenlandskbank"]["banknavn"].asText() shouldBe HentPersonaliaTestData.banknavn
            personalia["utenlandskbank"]["kontonummer"].asText() shouldBe HentPersonaliaTestData.bankKontonummer
            personalia["utenlandskbank"]["land"].asText() shouldBe HentPersonaliaTestData.bankLand
            personalia["utenlandskbank"]["swiftkode"].asText() shouldBe HentPersonaliaTestData.bankSwiftkode
            personalia["utenlandskbank"]["valuta"].asText() shouldBe HentPersonaliaTestData.bankValuta

            personalia["tlfnr"]["telefonAlternativ"].asTextOrNull() shouldBe null
            personalia["tlfnr"]["landskodeAlternativ"].asTextOrNull() shouldBe null
            personalia["tlfnr"]["telefonHoved"].asText() shouldBe HentPersonaliaTestData.telefonnummer
            personalia["tlfnr"]["landskodeHoved"].asText() shouldBe HentPersonaliaTestData.telefonlandskode

            personalia["statsborgerskap"][0].asText() shouldBe HentPersonaliaTestData.statsborgerskap
            personalia["foedested"].asText() shouldBe HentPersonaliaTestData.foedested
            personalia["sivilstand"].asText() shouldBe HentPersonaliaTestData.sivilstand
            personalia["kjoenn"].asText() shouldBe HentPersonaliaTestData.kjoenn
        }

        responseJson["adresser"].let { adresser ->

            adresser["kontaktadresser"][0]["gyldigTilOgMed"].asTextOrNull() shouldBe null
            adresser["kontaktadresser"][0]["coAdressenavn"].asTextOrNull() shouldBe null
            adresser["kontaktadresser"][0]["kilde"].asText() shouldBe HentPersonaliaTestData.kontaktadresseKilde
            adresser["kontaktadresser"][0]["adresse"]["adresselinje1"].asText() shouldBe HentPersonaliaTestData.kontaktadresseAdresselinje1
            adresser["kontaktadresser"][0]["adresse"]["adresselinje2"].asText() shouldBe HentPersonaliaTestData.kontaktadresseAdresselinje2
            adresser["kontaktadresser"][0]["adresse"]["adresselinje3"].asText() shouldBe HentPersonaliaTestData.kontaktadresseAdresselinje3
            adresser["kontaktadresser"][0]["adresse"]["postnummer"].asTextOrNull() shouldBe null
            adresser["kontaktadresser"][0]["adresse"]["poststed"].asTextOrNull() shouldBe null
            adresser["kontaktadresser"][0]["adresse"]["type"].asText() shouldBe HentPersonaliaTestData.kontaktadresseType

            adresser["bostedsadresse"]["angittFlyttedato"].asTextOrNull() shouldBe null
            adresser["bostedsadresse"]["coAdressenavn"].asTextOrNull() shouldBe null
            adresser["bostedsadresse"]["adresse"]["husnummer"].asText() shouldBe HentPersonaliaTestData.bostedsadresseHusnummer
            adresser["bostedsadresse"]["adresse"]["husbokstav"].asTextOrNull() shouldBe null
            adresser["bostedsadresse"]["adresse"]["bruksenhetsnummer"].asTextOrNull() shouldBe null
            adresser["bostedsadresse"]["adresse"]["adressenavn"].asText() shouldBe HentPersonaliaTestData.bostedsadresseAdressenavn
            adresser["bostedsadresse"]["adresse"]["kommune"].asText() shouldBe HentPersonaliaTestData.bostedsadresseKommune
            adresser["bostedsadresse"]["adresse"]["tilleggsnavn"].asTextOrNull() shouldBe null
            adresser["bostedsadresse"]["adresse"]["postnummer"].asText() shouldBe HentPersonaliaTestData.bostedsadressePostnummer
            adresser["bostedsadresse"]["adresse"]["poststed"].asText() shouldBe HentPersonaliaTestData.bostedsadressePoststed
            adresser["bostedsadresse"]["adresse"]["type"].asText() shouldBe HentPersonaliaTestData.bostedsadresseType

            adresser["oppholdsadresser"].size() shouldBe 0
            adresser["deltBosted"].asTextOrNull() shouldBe null
        }
    }

    @Test
    fun `bruker riktige headers mot pdl-api`() = apiTest(internalRouteConfig) {

        setupDefaultExternalRoutes(setupPdl = false)

        var headers: Headers? = null

        externalService(pdlApiUrl) {
            post("/graphql") {
                headers = call.request.headers
                call.respondText(ExternalResponse.pdlHentPerson, contentType = ContentType.Application.Json)
            }
        }

        client.get(hentPersonaliaPath)

        headers.shouldNotBeNull().let {
            it[HttpHeaders.Authorization] shouldBe "Bearer $pdlApiToken"
            it[HeaderHelper.CALL_ID_HEADER].shouldNotBeNull()
            it[HeaderHelper.NAV_CONSUMER_ID_HEADER] shouldBe HeaderHelper.NAV_CONSUMER_ID
            it["Behandlingsnummer"] shouldBe behandlingsnummer
        }
    }

    @Test
    fun `bruker riktige headers mot norg2`() = apiTest(internalRouteConfig) {

        setupDefaultExternalRoutes(setupNorg2 = false)

        var headersEnhet: Headers? = null
        var headersKontaktinfo: Headers? = null


        externalService(norg2Url) {
            get("/api/v1/enhet/navkontor/{geografiskId}") {
                if (call.pathParameters["geografiskId"] == HentPersonaliaTestData.geografiskTilknytning) {
                    headersEnhet = call.request.headers
                    call.respondText(ExternalResponse.norg2Enhet, contentType = ContentType.Application.Json)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            get("/api/v2/enhet/{enhetNr}/kontaktinformasjon") {
                if (call.pathParameters["enhetNr"] == HentPersonaliaTestData.enhetsnummer) {
                    headersKontaktinfo = call.request.headers
                    call.respondText(ExternalResponse.norg2Kontaktinfo, contentType = ContentType.Application.Json)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }

        client.get(hentPersonaliaPath)

        headersEnhet.shouldNotBeNull().let {
            it[HttpHeaders.Authorization].shouldBeNull()
            it[HeaderHelper.CALL_ID_HEADER].shouldNotBeNull()
            it[HeaderHelper.NAV_CONSUMER_ID_HEADER] shouldBe HeaderHelper.NAV_CONSUMER_ID
        }

        headersKontaktinfo.shouldNotBeNull().let {
            it[HttpHeaders.Authorization].shouldBeNull()
            it[HeaderHelper.CALL_ID_HEADER].shouldNotBeNull()
            it[HeaderHelper.NAV_CONSUMER_ID_HEADER] shouldBe HeaderHelper.NAV_CONSUMER_ID
        }
    }

    @Test
    fun `bruker riktige headers mot kontoregister`() = apiTest(internalRouteConfig) {

        setupDefaultExternalRoutes(setupKontoregister = false)

        var headersAktivKonto: Headers? = null
        var headersLandkoder: Headers? = null
        var headersValutakoder: Headers? = null

        externalService(kontoregisterUrl) {
            post("/api/borger/v1/hent-aktiv-konto") {
                headersAktivKonto = call.request.headers
                call.respondText(ExternalResponse.kontoregisterKonto, contentType = ContentType.Application.Json)
            }

            get("/api/system/v1/hent-landkoder") {
                headersLandkoder = call.request.headers
                call.respondText(ExternalResponse.kontoregisterLandkoder, contentType = ContentType.Application.Json)
            }

            get("/api/system/v1/hent-valutakoder") {
                headersValutakoder = call.request.headers
                call.respondText(ExternalResponse.kontoregisterValutakoder, contentType = ContentType.Application.Json)
            }
        }

        client.get(hentPersonaliaPath)

        headersAktivKonto.shouldNotBeNull().let {
            it[HttpHeaders.Authorization] shouldBe "Bearer $kontoregisterToken"
            it[HeaderHelper.CALL_ID_HEADER].shouldNotBeNull()
            it[HeaderHelper.NAV_CONSUMER_ID_HEADER] shouldBe HeaderHelper.NAV_CONSUMER_ID
        }

        headersLandkoder.shouldNotBeNull().let {
            it[HttpHeaders.Authorization].shouldBeNull()
            it[HeaderHelper.CALL_ID_HEADER].shouldBeNull()
            it[HeaderHelper.NAV_CONSUMER_ID_HEADER].shouldBeNull()
        }

        headersValutakoder.shouldNotBeNull().let {
            it[HttpHeaders.Authorization].shouldBeNull()
            it[HeaderHelper.CALL_ID_HEADER].shouldBeNull()
            it[HeaderHelper.NAV_CONSUMER_ID_HEADER].shouldBeNull()
        }
    }

    @Test
    fun `bruker riktige headers mot kodeverk`() = apiTest(internalRouteConfig) {

        setupDefaultExternalRoutes(setupKontoregister = false)

        var headersLandkoder: Headers? = null
        var headersKommuner: Headers? = null
        var headersStatsborgerskapFreg: Headers? = null
        var headersPostnummer: Headers? = null

        externalService(kodeverkUrl) {
            get("/api/v1/kodeverk/Landkoder/koder/betydninger") {
                headersLandkoder = call.request.headers
                call.respondText(ExternalResponse.kodeverkLandkoder, contentType = ContentType.Application.Json)
            }

            get("/api/v1/kodeverk/Kommuner/koder/betydninger") {
                headersKommuner = call.request.headers
                call.respondText(ExternalResponse.kodeverkKommuner, contentType = ContentType.Application.Json)
            }

            get("/api/v1/kodeverk/StatsborgerskapFreg/koder/betydninger") {
                headersStatsborgerskapFreg = call.request.headers
                call.respondText(ExternalResponse.kodeverkStatsborgerskap, contentType = ContentType.Application.Json)
            }

            get("/api/v1/kodeverk/Postnummer/koder/betydninger") {
                headersPostnummer = call.request.headers
                call.respondText(ExternalResponse.kodeverkPostnummer, contentType = ContentType.Application.Json)
            }
        }

        client.get(hentPersonaliaPath)

        headersLandkoder.shouldNotBeNull().let {
            it[HttpHeaders.Authorization] shouldBe "Bearer $kodeverkToken"
            it[HeaderHelper.CALL_ID_HEADER].shouldNotBeNull()
            it[HeaderHelper.NAV_CONSUMER_ID_HEADER] shouldBe HeaderHelper.NAV_CONSUMER_ID
        }

        headersKommuner.shouldNotBeNull().let {
            it[HttpHeaders.Authorization] shouldBe "Bearer $kodeverkToken"
            it[HeaderHelper.CALL_ID_HEADER].shouldNotBeNull()
            it[HeaderHelper.NAV_CONSUMER_ID_HEADER] shouldBe HeaderHelper.NAV_CONSUMER_ID
        }

        headersStatsborgerskapFreg.shouldNotBeNull().let {
            it[HttpHeaders.Authorization] shouldBe "Bearer $kodeverkToken"
            it[HeaderHelper.CALL_ID_HEADER].shouldNotBeNull()
            it[HeaderHelper.NAV_CONSUMER_ID_HEADER] shouldBe HeaderHelper.NAV_CONSUMER_ID
        }

        headersPostnummer.shouldNotBeNull().let {
            it[HttpHeaders.Authorization] shouldBe "Bearer $kodeverkToken"
            it[HeaderHelper.CALL_ID_HEADER].shouldNotBeNull()
            it[HeaderHelper.NAV_CONSUMER_ID_HEADER] shouldBe HeaderHelper.NAV_CONSUMER_ID
        }
    }

    @Test
    fun `feiler hvis pdl er nede`() = apiTest(internalRouteConfig) {
        setupDefaultExternalRoutes(setupPdl = false)

        client.get(hentPersonaliaPath).status shouldBe HttpStatusCode.InternalServerError
    }

    @Test
    fun `feiler hvis norg2 er nede`() = apiTest(internalRouteConfig) {
        setupDefaultExternalRoutes(setupNorg2 = false)

        client.get(hentPersonaliaPath).status shouldBe HttpStatusCode.InternalServerError
    }

    @Test
    fun `goddtar at kontoregister er nede`() = apiTest(internalRouteConfig) {
        setupDefaultExternalRoutes(setupKontoregister = false)

        client.get(hentPersonaliaPath).status shouldBe HttpStatusCode.OK
    }

    @Test
    fun `feiler hvis kodeverk er nede`() = apiTest(internalRouteConfig) {
        setupDefaultExternalRoutes(setupKodeverk = false)

        client.get(hentPersonaliaPath).status shouldBe HttpStatusCode.InternalServerError
    }
}
