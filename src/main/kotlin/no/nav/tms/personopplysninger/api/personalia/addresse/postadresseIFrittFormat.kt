package no.nav.tms.personopplysninger.api.personalia.addresse

import no.nav.pdl.generated.dto.hentpersonquery.PostadresseIFrittFormat as PdlPostadresseIFrittFormat

data class PostadresseIFrittFormat(
    val adresselinje1: String?,
    val adresselinje2: String?,
    val adresselinje3: String?,
    val postnummer: String?,
    val poststed: String?
) : Adresse {
    override val type: Adressetype get() = Adressetype.POSTADRESSE_I_FRITT_FORMAT

    companion object {
        fun mapAdresse(postadresseIFrittFormat: PdlPostadresseIFrittFormat, poststed: String?) =
            PostadresseIFrittFormat(
                adresselinje1 = postadresseIFrittFormat.adresselinje1,
                adresselinje2 = postadresseIFrittFormat.adresselinje2,
                adresselinje3 = postadresseIFrittFormat.adresselinje3,
                postnummer = postadresseIFrittFormat.postnummer,
                poststed = poststed,
            )
    }
}
