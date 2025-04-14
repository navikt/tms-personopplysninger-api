package no.nav.tms.personopplysninger.api.personalia

data class Konto(
    val kontonummer: String? = null,
    val utenlandskKontoInfo: UtenlandskKontoInfo? = null,
    val error: Boolean = false
)

data class UtenlandskKontoInfo(
    val banknavn: String = "",
    val bankkode: String? = null,
    val bankLandkode: String = "",
    val valutakode: String,
    val swiftBicKode: String? = null,
    val bankadresse1: String = "",
    val bankadresse2: String = "",
    val bankadresse3: String = "",
) {
    companion object {
        fun from(utenlandskKontoInformasjon: KontoResponse.UtenlandskKontoInformasjon): UtenlandskKontoInfo {
            return UtenlandskKontoInfo(
                banknavn = utenlandskKontoInformasjon.bank?.navn.orEmpty(),
                bankkode = utenlandskKontoInformasjon.bank?.kode,
                bankLandkode = utenlandskKontoInformasjon.landkode.orEmpty(),
                valutakode = utenlandskKontoInformasjon.valuta,
                swiftBicKode = utenlandskKontoInformasjon.swift,
                bankadresse1 = utenlandskKontoInformasjon.bank?.adresseLinje1.orEmpty(),
                bankadresse2 = utenlandskKontoInformasjon.bank?.adresseLinje2.orEmpty(),
                bankadresse3 = utenlandskKontoInformasjon.bank?.adresseLinje3.orEmpty(),
            )
        }
    }
}

data class HentAktivKonto(
    val kontohaver: String
)

object KontoResponse {
    data class Bank(
        val adresseLinje1: String? = null,
        val adresseLinje2: String? = null,
        val adresseLinje3: String? = null,
        val kode: String? = null,
        val navn: String? = null,
    )

    data class Kontonummer(
        val kilde: String = "BRUKER SELV",
        val utenlandskKontoInformasjon: UtenlandskKontoInformasjon? = null,
        val value: String,
    )

    data class Landkode(
        val landkode: String,
        val land: String,
        val kreverIban: Boolean,
        val ibanLengde: Int? = null,
        val kreverBankkode: Boolean,
        val bankkodeLengde: Int? = null,
        val alternativLandkode: String? = null
    )

    data class UtenlandskKontoInformasjon (
        val bank: Bank? = null,
        val landkode: String? = null,
        val swift: String? = null,
        val valuta: String,
    )

    data class ValidationError(
        val feilmelding: String,
    )

    data class Valutakode(
        val valutakode: String,
        val valuta: String,
    )
}
