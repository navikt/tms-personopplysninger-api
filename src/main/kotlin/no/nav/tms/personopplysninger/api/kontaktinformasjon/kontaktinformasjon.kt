package no.nav.tms.personopplysninger.api.kontaktinformasjon

data class Kontaktinformasjon(
    val epostadresse: String? = null,
    val mobiltelefonnummer: String? = null,
    val reservert: Boolean? = null,
    val spraak: String? = null
) {
    companion object {
        fun fromDigitalInfo(info: DigitalKontaktinformasjon, spraakTerm: String?) = Kontaktinformasjon(
            epostadresse = info.epostadresse,
            mobiltelefonnummer = info.mobiltelefonnummer,
            reservert = info.reservert,
            spraak = if (spraakTerm == "Norsk") "Bokmål" else spraakTerm
        )
    }
}

data class DigitalKontaktinformasjon (
    val personident: String? = null,
    val aktiv: Boolean,
    val kanVarsles: Boolean? = null,
    val reservert: Boolean? = null,
    val spraak: String? = null,
    val epostadresse: String? = null,
    val mobiltelefonnummer: String? = null,
    val sikkerDigitalPostkasse: SikkerDigitalPostkasse? = null,
)

data class SikkerDigitalPostkasse (
    val adresse: String? = null,
    val leverandoerAdresse: String? = null,
    val leverandoerSertifikat: String? = null,
)

