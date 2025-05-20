package no.nav.tms.personopplysninger.api.personalia.norg2

data class Norg2Enhet(
    val enhetNr: String = "",
)

data class Norg2EnhetKontaktinfo(
    val navn: String,
    val brukerkontakt: Brukerkontakt,
)

data class Brukerkontakt(
    val publikumsmottak: List<Publikumsmottak>
)

data class Publikumsmottak(
    val besoeksadresse: Adresse?,
    val aapningstider: List<Aapningstider>?,
    val stedsbeskrivelse: String?,
    val adkomstbeskrivelse: String?,
)

data class Adresse(
    val postnummer: String?,
    val poststed: String?,
    val gatenavn: String?,
    val husnummer: String?,
    val husbokstav: String?,
    val adresseTilleggsnavn: String?,
)

data class Aapningstider(
    val dag: String?,
    val dato: String?,
    val fra: String?,
    val til: String?,
    val kommentar: String?,
    val stengt: String?,
    val kunTimeavtale: String?,
)
