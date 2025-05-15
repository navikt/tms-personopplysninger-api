package no.nav.tms.personopplysninger.api.personalia

import no.nav.tms.personopplysninger.api.RouteTest

object HentPersonaliaTestData {

    // `personalia`
    const val ident = RouteTest.testIdent
    const val identtype = "FNR"

    const val fornavn = "TEST TESTER"
    const val etternavn = "TESTEST"

    const val statsborgerskap = "NORGE"
    const val foedested = "Bergen, NORGE"
    const val sivilstand = "Gift"
    const val kjoenn = "Mann"

    const val kontoregisterStatus = "SUCCESS"
    const val bankAdresse1 = "Bankveien 2"
    const val bankAdresse2 = "0000 Bankstad"
    const val bankAdresse3 = "Sverige"
    const val bankkode = "CC000000000"
    const val banknavn = "Banken Bank"
    const val bankKontonummer = "0000111122223333"
    const val bankLand = "Sverige"
    const val bankSwiftkode = "SWEDEN00"
    const val bankValuta = "Svensk krone"

    const val telefonlandskode = "+47"
    const val telefonnummer = "12345678"

    // `adresser`
    const val kontaktadresseKilde = "pdl"
    const val kontaktadresseAdresselinje1 = "Hylkjelia"
    const val kontaktadresseAdresselinje2 = "5109 HYLKJE"
    const val kontaktadresseAdresselinje3 = "Norge"
    const val kontaktadresseType = "POSTADRESSE_I_FRITT_FORMAT"

    const val bostedsadresseHusnummer = "100"
    const val bostedsadresseAdressenavn = "Almåsvegen"
    const val bostedsadresseKommune = "Bergen"
    const val bostedsadressePostnummer = "5109"
    const val bostedsadressePoststed = "HYLKJE"
    const val bostedsadresseType = "VEGADRESSE"

    // `enhetskontaktinformasjon`
    const val geografiskTilknytning = "460108"
    const val enhetsnummer = "0101"

    const val enhetsKontaktinformasjon = "NAV Halden-Aremark"
    const val brukerkontaktPostnummer = "1798"
    const val brukerkontaktPoststed = "AREMARK"
    const val brukerkontaktGatenavn = "Aremarkveien"
    const val brukerkontaktHusnummer = "2276"
    const val brukerkontaktStedsbeskrivelse = "Aremark"

    const val brukerkontaktAapningstidDag = "Fredag"
    const val brukerkontaktAapningstidKommentar = "Timeavtaler i tidsrommet 09.00 - 15.00."
    const val brukerkontaktAapningstidStengt = "true"
    const val brukerkontaktAapningstidKunTimeavtale = "false"

    object ExternalResponse {

        const val pdlHentPerson = """
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
          "identifikasjonsnummer": "$ident",
          "type": "FNR"
        }
      ],
      "statsborgerskap": [
        {
          "land": "NOR"
        },
        {
          "land": "SWE",
          "gyldigTilOgMed": "2020-01-01"
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
          "type": "GIFT",
          "gyldigFraOgMed": "2020-01-01"
        },
        {
          "type": "UGIFT",
          "gyldigFraOgMed": "2000-01-01"
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

        const val norg2Enhet = """
{
    "enhetNr": "0101"
}
"""

        const val norg2Kontaktinfo = """
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
          "postnummer": "1798",
          "poststed": "AREMARK",
          "gatenavn": "Aremarkveien",
          "husnummer": "2276",
          "husbokstav": null,
          "adresseTilleggsnavn": null
        },
        "aapningstider": [
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

        const val kontoregisterKonto = """
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

        const val kontoregisterLandkoder = """
[
  {
    "landkode" : "NO",
    "land" : "Norge",
    "kreverIban" : true,
    "ibanLengde" : 15,
    "kreverBankkode" : false
  },
  {
    "landkode" : "SE",
    "land" : "Sverige",
    "kreverIban" : true,
    "ibanLengde" : 24,
    "kreverBankkode" : false
  }
]
"""

        const val kontoregisterValutakoder = """
[ 
    {
      "valutakode" : "NOK",
      "valuta" : "Norsk krone"
    },
    {
      "valutakode" : "SEK",
      "valuta" : "Svensk krone"
    }
]
"""

        const val kodeverkKommuner = """
{
  "betydninger": {
    "4601": [
      {
        "gyldigFra": "1900-01-01",
        "gyldigTil": "9999-12-31",
        "beskrivelser": {
          "nb": {
            "term": "Bergen",
            "tekst": "Bergen"
          }
        }
      }
    ],
    "0301": [
      {
        "gyldigFra": "1900-01-01",
        "gyldigTil": "9999-12-31",
        "beskrivelser": {
          "nb": {
            "term": "Oslo",
            "tekst": "Oslo"
          }
        }
      }
    ]
  }
}
"""

        const val kodeverkLandkoder = """
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
"""

        const val kodeverkStatsborgerskap = """
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
"""

        const val kodeverkPostnummer = """
{
  "betydninger": {
    "1751": [
      {
        "gyldigFra": "1900-01-01",
        "gyldigTil": "9999-12-31",
        "beskrivelser": {
          "nb": {
            "term": "HALDEN"
          }
        }
      }
    ],
    "1771": [
      {
        "gyldigFra": "1900-01-01",
        "gyldigTil": "9999-12-31",
        "beskrivelser": {
          "nb": {
            "term": "SVERIGE"
          }
        }
      }
    ],
    "1798": [
      {
        "gyldigFra": "1900-01-01",
        "gyldigTil": "9999-12-31",
        "beskrivelser": {
          "nb": {
            "term": "AREMARK"
          }
        }
      }
    ],
    "5109": [
      {
        "gyldigFra": "1900-01-01",
        "gyldigTil": "9999-12-31",
        "beskrivelser": {
          "nb": {
            "term": "HYLKJE"
          }
        }
      }
    ]
  }
}
"""
    }
}
