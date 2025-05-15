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
import no.nav.tms.personopplysninger.api.RouteTest
import no.nav.tms.personopplysninger.api.InternalRouteConfig
import no.nav.tms.personopplysninger.api.common.HeaderHelper
import no.nav.tms.personopplysninger.api.common.TokenExchanger
import no.nav.tms.personopplysninger.api.kodeverk.KodeverkConsumer
import no.nav.tms.personopplysninger.api.kontoregister.KontoregisterConsumer
import no.nav.tms.personopplysninger.api.personalia.norg2.Norg2Consumer
import no.nav.tms.personopplysninger.api.personalia.norg2.Norg2Enhet
import no.nav.tms.personopplysninger.api.personalia.pdl.EndringsType
import no.nav.tms.personopplysninger.api.personalia.pdl.PdlApiConsumer
import no.nav.tms.personopplysninger.api.personalia.pdl.PendingEndring
import no.nav.tms.personopplysninger.api.personalia.pdl.Telefonnummer
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

    private val hentPersonaliaPath = "/personalia"

    @Test
    fun `henter personalia fra baktjenester`() = apiTest(internalRouteConfig) { client ->

        externalService(pdlApiUrl) {
            post("/graphql") {
                call.respondText(personaliaResponse, contentType = ContentType.Application.Json)
            }
        }

        externalService(norg2Url) {
            get("/api/v1/enhet/navkontor/{geografiskId}") {
                if (call.pathParameters["geografiskId"] == "460108") {
                    call.respondText(norg2EnhetResponse, contentType = ContentType.Application.Json)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            get("/api/v2/enhet/{enhetNr}/kontaktinformasjon") {
                if (call.pathParameters["enhetNr"] == "0101") {
                    call.respondText(norg2KontaktinfoResponse, contentType = ContentType.Application.Json)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }

        externalService()

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

    private val personaliaResponse = """
{
  "data": {
    "person": {
      "navn": [
        {
          "fornavn": "TEST",
          "mellomnavn": "TESTER",
          "etternavn": "TESTEST"
        }
      ],
      "telefonnummer": [
        {
          "landskode": "+47",
          "nummer": "12345678",
          "prioritet": 1,
          "metadata": {
            "opplysningsId": "123"
          }
        }
      ],
      "folkeregisteridentifikator": [
        {
          "identifikasjonsnummer": "$testIdent",
          "type": "FNR"
        }
      ],
      "statsborgerskap": [
        {
          "land": "NOR"
        },
        {
          "land": "SYR",
          "gyldigTilOgMed": "2020-02-28"
        }
      ],
      "foedested": [
        {
          "foedekommune": "4601",
          "foedeland": "NOR"
        }
      ],
      "sivilstand": [
        {
          "type": "UGIFT",
          "gyldigFraOgMed": "2000-01-01"
        },
        {
          "type": "GIFT",
          "gyldigFraOgMed": "2020-01-01"
        }
      ],
      "kjoenn": [
        {
          "kjoenn": "MANN"
        }
      ],
      "bostedsadresse": [
        {
          "angittFlyttedato": null,
          "gyldigFraOgMed": "2020-01-01T12:00:00",
          "gyldigTilOgMed": null,
          "coAdressenavn": null,
          "vegadresse": {
            "husnummer": "100",
            "husbokstav": null,
            "bruksenhetsnummer": null,
            "adressenavn": "Almåsvegen",
            "kommunenummer": "4601",
            "bydelsnummer": null,
            "tilleggsnavn": null,
            "postnummer": "5109"
          },
          "matrikkeladresse": null,
          "utenlandskAdresse": null,
          "ukjentBosted": null,
          "metadata": {
            "opplysningsId": "456",
            "master": "Freg"
          }
        }
      ],
      "deltBosted": [],
      "kontaktadresse": [
        {
          "gyldigFraOgMed": "2020-03-24T00:00",
          "gyldigTilOgMed": null,
          "type": "Innland",
          "coAdressenavn": null,
          "postboksadresse": null,
          "vegadresse": null,
          "postadresseIFrittFormat": {
            "adresselinje1": "Hylkjelia",
            "adresselinje2": "5109 HYLKJE",
            "adresselinje3": "Norge",
            "postnummer": null
          },
          "utenlandskAdresse": null,
          "utenlandskAdresseIFrittFormat": null,
          "folkeregistermetadata": {
            "ajourholdstidspunkt": null,
            "gyldighetstidspunkt": "2020-03-24T00:00",
            "opphoerstidspunkt": null,
            "kilde": "KILDE_DSF",
            "aarsak": null,
            "sekvens": null
          },
          "metadata": {
            "opplysningsId": "789",
            "master": "pdl"
          }
        }
      ],
      "oppholdsadresse": []
    },
    "geografiskTilknytning": {
      "gtKommune": null,
      "gtBydel": "460108"
    }
  }
}
"""

    private val norg2EnhetResponse = """
{
    "enhetNr": "0101"
}
"""

    private val norg2KontaktinfoResponse = """
{
  "enhetNr": "0101",
  "navn": "NAV Halden-Aremark",
  "telefonnummer": "55553333",
  "telefonnummerKommentar": null,
  "epost": null,
  "postadresse": {
    "type": "postboksadresse",
    "postnummer": "1751",
    "poststed": "HALDEN",
    "postboksnummer": "56",
    "postboksanlegg": null
  },
  "besoeksadresse": {
    "type": "stedsadresse",
    "postnummer": "1771",
    "poststed": "HALDEN",
    "gatenavn": "Storgata",
    "husnummer": "8",
    "husbokstav": null,
    "adresseTilleggsnavn": null
  },
  "spesielleOpplysninger": "NAV Halden - Aremark har digital drop-in og er tilgjengelig via NAV sitt telefonnummer. Vi har samtaler ved fremmøte der det er behov for det og samhandler med våre innbyggere på de arenaer som er hensiktsmessige.\n\nFor deg som har mulighet, fortsett å benytt deg av våre tjenester på www.nav.no, «Skriv til oss» på Ditt NAV eller dialogen i aktivitetsplan. Alle våre veiledere er tilgjengelige på digitale løsninger.\n\nDersom du er i en akutt situasjon, f.eks. uten bolig, mat eller livsviktige medisiner, send inn digital søknad og merk denne «Nødhjelp».  Kan du ikke søke digitalt, kontakt oss på telefon. Halden kommunes servicesenter kan i nødtilfeller låne deg telefon, dersom du ikke disponerer det.",
  "brukerkontakt": {
    "publikumsmottak": [
      {
        "besoeksadresse": {
          "type": "stedsadresse",
          "postnummer": "1771",
          "poststed": "HALDEN",
          "gatenavn": "Storgata",
          "husnummer": "8",
          "husbokstav": null,
          "adresseTilleggsnavn": null
        },
        "aapningstider": [
          {
            "dag": null,
            "dato": "2023-03-31",
            "fra": null,
            "til": null,
            "kommentar": "Steng fordi Bjørnar er i Granca",
            "stengt": true,
            "kunTimeavtale": false
          }
        ],
        "stedsbeskrivelse": "Halden",
        "adkomstbeskrivelse": null
      },
      {
        "besoeksadresse": {
          "type": "stedsadresse",
          "postnummer": "1798",
          "poststed": "AREMARK",
          "gatenavn": "Aremarkveien",
          "husnummer": "2276",
          "husbokstav": null,
          "adresseTilleggsnavn": null
        },
        "aapningstider": [
          {
            "dag": "Mandag",
            "dato": null,
            "fra": null,
            "til": null,
            "kommentar": null,
            "stengt": false,
            "kunTimeavtale": false
          },
          {
            "dag": "Fredag",
            "dato": null,
            "fra": null,
            "til": null,
            "kommentar": "Timeavtaler i tidsrommet 09.00 - 15.00.",
            "stengt": true,
            "kunTimeavtale": false
          }
        ],
        "stedsbeskrivelse": "Aremark",
        "adkomstbeskrivelse": null
      }
    ]
  }
}
"""

    private val kontoregisterResponse = """
{
  "kontonummer": "0000111122223333",
  "utenlandskKontoInfo": {
    "banknavn": "Banken Bank",
    "bankkode": "CC000000000",
    "bankLandkode": "SE",
    "valutakode": "SEK",
    "swiftBicKode": "SWEDEN00",
    "bankadresse1": "Bankveien 2",
    "bankadresse2": "0000 Bankstad",
    "bankadresse3": "Sverige"
  }
}
"""

    private val kodeverkKommunerResponse = """
{
  "betydninger": {
    "4601": [
      {
        "gyldigFra": "2019-12-27",
        "gyldigTil": "9999-12-31",
        "beskrivelser": {
          "nb": {
            "term": "Indre Østfold",
            "tekst": "Indre Østfold"
          }
        }
      }
    ],
    "5434": [
      {
        "gyldigFra": "2019-12-27",
        "gyldigTil": "9999-12-31",
        "beskrivelser": {
          "nb": {
            "term": "Måsøy",
            "tekst": "Måsøy"
          }
        }
      }
    ],
    "5435": [
      {
        "gyldigFra": "2019-12-27",
        "gyldigTil": "9999-12-31",
        "beskrivelser": {
          "nb": {
            "term": "Nordkapp",
            "tekst": "Nordkapp"
          }
        }
      }
    ]
  }
}
"""
}
