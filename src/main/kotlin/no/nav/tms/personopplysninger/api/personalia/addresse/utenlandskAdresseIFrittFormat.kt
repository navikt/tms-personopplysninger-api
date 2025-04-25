package no.nav.tms.personopplysninger.api.personalia.addresse

import no.nav.pdl.generated.dto.hentpersonquery.UtenlandskAdresseIFrittFormat as PdlUtenlandskAdresseIFrittFormat

data class UtenlandskAdresseIFrittFormat(
    val adresselinje1: String?,
    val adresselinje2: String?,
    val adresselinje3: String?,
    val land: String?
) : Adresse {
    override val type: Adressetype get() = Adressetype.UTENLANDSK_ADRESSE_I_FRITT_FORMAT

    companion object {
        fun mapAdresse(utenlandskAdresseIFrittFormat: PdlUtenlandskAdresseIFrittFormat, land: String?) =
            UtenlandskAdresseIFrittFormat(
                adresselinje1 = utenlandskAdresseIFrittFormat.adresselinje1,
                adresselinje2 = utenlandskAdresseIFrittFormat.adresselinje2,
                adresselinje3 = utenlandskAdresseIFrittFormat.adresselinje3,
                land = land,
            )
    }
}
