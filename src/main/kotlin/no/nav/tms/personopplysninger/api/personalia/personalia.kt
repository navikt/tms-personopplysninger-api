package no.nav.tms.personopplysninger.api.personalia

data class Personalia(
    val fornavn: String? = null,
    val etternavn: String? = null,
    val personident: Personident? = null,
    val kontoregisterStatus: String = "SUCCESS",
    val kontonr: String? = null,
    val utenlandskbank: UtenlandskBankInfo? = null,
    val tlfnr: Tlfnr? = null,
    val statsborgerskap: List<String> = emptyList(),
    val foedested: String? = null,
    val sivilstand: String? = null,
    val kjoenn: String? = null
)

data class Personident(val verdi: String, val type: String?)

data class Tlfnr (
    /* Telefonnummer jobb */
    val telefonAlternativ: String? = null,
    val landskodeAlternativ: String? = null,
    /* Telefonnummer mobil */
    val telefonHoved: String? = null,
    val landskodeHoved: String? = null
)

data class UtenlandskBankInfo(
    val adresse1: String? = null,
    val adresse2: String? = null,
    val adresse3: String? = null,
    val bankkode: String? = null,
    val banknavn: String? = null,
    val kontonummer: String? = null,
    val land: String? = null,
    val swiftkode: String? = null,
    val valuta: String? = null
)
