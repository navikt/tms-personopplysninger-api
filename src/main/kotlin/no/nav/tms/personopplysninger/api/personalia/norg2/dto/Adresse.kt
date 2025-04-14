package no.nav.tms.personopplysninger.api.personalia.norg2.dto


data class Adresse(
    val postnummer: String?,
    val poststed: String?,
    val gatenavn: String?,
    val husnummer: String?,
    val husbokstav: String?,
    val adresseTilleggsnavn: String?,
)
