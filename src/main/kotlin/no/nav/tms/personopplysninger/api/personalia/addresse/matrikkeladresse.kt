package no.nav.tms.personopplysninger.api.personalia.addresse

import no.nav.pdl.generated.dto.hentpersonquery.Matrikkeladresse as PdlMatrikkeladresse

data class Matrikkeladresse(
    val bruksenhetsnummer: String?,
    val tilleggsnavn: String?,
    val postnummer: String?,
    val poststed: String?,
    val kommune: String?,
) : Adresse {
    override val type: Adressetype get() = Adressetype.MATRIKKELADRESSE

    companion object {
        fun mapAdresse(utenlandskAdresse: PdlMatrikkeladresse, poststed: String?, kommune: String?) =
            Matrikkeladresse(
                bruksenhetsnummer = utenlandskAdresse.bruksenhetsnummer,
                tilleggsnavn = utenlandskAdresse.tilleggsnavn,
                postnummer = utenlandskAdresse.postnummer,
                poststed = poststed,
                kommune = kommune,
            )
    }
}
