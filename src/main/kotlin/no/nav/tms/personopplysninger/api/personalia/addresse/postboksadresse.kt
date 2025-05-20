package no.nav.tms.personopplysninger.api.personalia.addresse

import no.nav.pdl.generated.dto.hentpersonquery.Postboksadresse as PdlPostboksadresse

data class Postboksadresse(
    val postbokseier: String?,
    val postboks: String,
    val postnummer: String?,
    val poststed: String?
) : Adresse {
    override val type: Adressetype get() = Adressetype.POSTBOKSADRESSE

    companion object {
        fun mapAdresse(postboksadresse: PdlPostboksadresse, poststed: String?): Postboksadresse {
            return Postboksadresse(
                postbokseier = postboksadresse.postbokseier,
                postboks = postboksadresse.postboks,
                postnummer = postboksadresse.postnummer,
                poststed = poststed,
            )
        }
    }
}
