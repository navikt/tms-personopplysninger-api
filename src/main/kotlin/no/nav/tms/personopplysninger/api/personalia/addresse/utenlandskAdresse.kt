package no.nav.tms.personopplysninger.api.personalia.addresse

import no.nav.pdl.generated.dto.hentpersonquery.UtenlandskAdresse as PdlUtenlandskAdresse

data class UtenlandskAdresse(
    val adressenavnNummer: String?,
    val bygningEtasjeLeilighet: String?,
    val postboksNummerNavn: String?,
    val postkode: String?,
    val bySted: String?,
    val regionDistriktOmraade: String?,
    val land: String?
) : Adresse {
    override val type: Adressetype get() = Adressetype.UTENLANDSK_ADRESSE

    companion object {
        fun mapAdresse(utenlandskAdresse: PdlUtenlandskAdresse, land: String?) =
            UtenlandskAdresse(
                adressenavnNummer = utenlandskAdresse.adressenavnNummer,
                bygningEtasjeLeilighet = utenlandskAdresse.bygningEtasjeLeilighet,
                postboksNummerNavn = utenlandskAdresse.postboksNummerNavn,
                postkode = utenlandskAdresse.postkode,
                bySted = utenlandskAdresse.bySted,
                regionDistriktOmraade = utenlandskAdresse.regionDistriktOmraade,
                land = land,
            )
    }
}
