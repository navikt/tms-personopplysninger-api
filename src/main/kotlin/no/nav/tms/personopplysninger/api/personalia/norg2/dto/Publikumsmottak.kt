package no.nav.tms.personopplysninger.api.personalia.norg2.dto

data class Publikumsmottak(
    val besoeksadresse: Adresse?,
    val aapningstider: List<Aapningstider>?,
    val stedsbeskrivelse: String?,
    val adkomstbeskrivelse: String?,
)
