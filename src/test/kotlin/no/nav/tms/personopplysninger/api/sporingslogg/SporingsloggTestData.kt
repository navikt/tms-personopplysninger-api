package no.nav.tms.personopplysninger.api.sporingslogg

import java.time.LocalDateTime

object SporingsloggTestData {

    const val tema = "Fiktivt tema"
    const val mottaker = "123456789"
    const val mottakernavn = "ARBEIDS- OG VELFERDSETATEN DUMMY DRIFT"
    const val leverteData = "eyJlYXN0ZXIiOiAiZWdnIn0K"
    const val samtykkeToken = "<samtykketoken>"
    val uthentingsTidspunkt = LocalDateTime.parse("2020-01-03T00:00:00")

    object ExternalResponse {
        const val eregServices = """
{
  "organisasjonsnummer": "123456789",
  "navn": {
    "redigertnavn": "ARBEIDS- OG VELFERDSETATEN",
    "navnelinje1": "ARBEIDS- OG VELFERDSETATEN",
    "navnelinje3": "DUMMY DRIFT",
    "bruksperiode": {
      "fom": "2020-01-02T00:00:00"
    },
    "gyldighetsperiode": {
      "fom": "2020-01-01"
    }
  },
  "enhetstype": "BEDR",
  "adresse": {
    "adresselinje1": "Gateveien 7",
    "postnummer": "0000",
    "landkode": "NO",
    "kommunenummer": "1111",
    "bruksperiode": {
      "fom": "2020-01-02T00:00:00"
    },
    "gyldighetsperiode": {
      "fom": "2020-01-01"
    }
  }
}
        """

        const val sporingslogg = """
[
  {
    "tema": "DUM",
    "uthentingsTidspunkt": "2020-01-03T00:00:00",
    "mottaker": "123456789",
    "leverteData": "eyJlYXN0ZXIiOiAiZWdnIn0K",
    "samtykkeToken": "<samtykketoken>"
  }
]
        """

        const val kodeverkTema = """
{
  "betydninger": {
    "DUM": [
      {
        "gyldigFra": "1900-01-01",
        "gyldigTil": "9999-12-31",
        "beskrivelser": {
          "nb": {
            "term": "Fiktivt tema"
          }
        }
      }
    ]
  }
}
"""
    }
}
