package no.nav.tms.personopplysninger.api.personalia.addresse

import no.nav.pdl.generated.dto.hentpersonquery.Vegadresse as PdlVegadresse

data class Vegadresse(
    val husnummer: String?,
    val husbokstav: String?,
    val bruksenhetsnummer: String?,
    val adressenavn: String?,
    val kommune: String?,
    val tilleggsnavn: String?,
    val postnummer: String?,
    val poststed: String?
) : Adresse {
    override val type: Adressetype get() = Adressetype.VEGADRESSE

    companion object {
        fun mapAdresse(vegadresse: PdlVegadresse, poststed: String?, kommune: String?) =
            Vegadresse(
                husnummer = vegadresse.husnummer,
                husbokstav = vegadresse.husbokstav,
                bruksenhetsnummer = vegadresse.bruksenhetsnummer,
                adressenavn = vegadresse.adressenavn,
                kommune = kommune,
                tilleggsnavn = vegadresse.tilleggsnavn,
                postnummer = vegadresse.postnummer,
                poststed = poststed,
            )
    }
}
