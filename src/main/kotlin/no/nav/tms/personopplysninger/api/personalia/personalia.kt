package no.nav.tms.personopplysninger.api.personalia

import no.nav.pdl.generated.dto.enums.KjoennType
import no.nav.pdl.generated.dto.enums.Sivilstandstype
import no.nav.pdl.generated.dto.hentpersonquery.Navn
import no.nav.pdl.generated.dto.hentpersonquery.Person
import no.nav.pdl.generated.dto.hentpersonquery.Telefonnummer
import no.nav.tms.personopplysninger.api.kodeverk.PersonaliaKodeverk
import no.nav.tms.personopplysninger.api.kontoregister.Konto
import no.nav.tms.personopplysninger.api.personalia.addresse.Adresser
import no.nav.tms.personopplysninger.api.personalia.norg2.Norg2EnhetKontaktinfo

data class PersonaliaOgAdresser(
    val personalia: Personalia,
    val adresser: Adresser?,
    val enhetKontaktInformasjon: Norg2EnhetKontaktinfo?
) {
    companion object {
        fun mapResult(
            person: Person,
            konto: Konto?,
            kodeverk: PersonaliaKodeverk,
            enhetKontaktInformasjon: Norg2EnhetKontaktinfo?
        ): PersonaliaOgAdresser {

            return PersonaliaOgAdresser(
                personalia = Personalia.mapPersonalia(
                    person,
                    konto,
                    kodeverk
                ),
                adresser = Adresser.mapAdresser(
                    person,
                    kodeverk
                ),
                enhetKontaktInformasjon
            )
        }
    }
}

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
) {
    companion object {
        fun mapPersonalia(
            person: Person,
            konto: Konto?,
            kodeverk: PersonaliaKodeverk
        ): Personalia {
            return Personalia(
                fornavn = person.navn.firstOrNull()?.fornavn(),
                etternavn = person.navn.firstOrNull()?.etternavn,
                personident = person.folkeregisteridentifikator.first().let { Personident(it.identifikasjonsnummer, it.type) },
                kontonr = konto?.kontonummer.takeIf { konto?.utenlandskKontoInfo == null },
                tlfnr = person.telefonnummer.toTlfnr(),
                utenlandskbank = konto?.utenlandskKontoInfo?.let { mapKonto(konto, kodeverk) },
                statsborgerskap = kodeverk.statsborgerskaptermer,
                foedested = foedested(kodeverk.foedekommuneterm, kodeverk.foedelandterm),
                sivilstand = person.sivilstand.firstOrNull()?.type?.beskrivelse,
                kjoenn = person.kjoenn.firstOrNull()?.kjoenn?.beskrivelse,
                kontoregisterStatus = if (konto?.error == true) "ERROR" else "SUCCESS"
            )
        }
    }
}

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


private fun mapKonto(konto: Konto, kodeverk: PersonaliaKodeverk): UtenlandskBankInfo {
    val utenlandskKontoInfo = requireNotNull(konto.utenlandskKontoInfo)

    return UtenlandskBankInfo(
        adresse1 = utenlandskKontoInfo.bankadresse1,
        adresse2 = utenlandskKontoInfo.bankadresse2,
        adresse3 = utenlandskKontoInfo.bankadresse3,
        bankkode = utenlandskKontoInfo.bankkode,
        banknavn = utenlandskKontoInfo.banknavn,
        kontonummer = konto.kontonummer,
        swiftkode = utenlandskKontoInfo.swiftBicKode,
        land = kodeverk.utenlandskbanklandterm,
        valuta = kodeverk.utenlandskbankvalutaterm,
    )
}

private fun Navn.fornavn() = if (mellomnavn == null) fornavn else "$fornavn $mellomnavn".trim()

private fun foedested(foedtIKommune: String?, foedtILand: String?): String? {
    val names = listOf(foedtIKommune, foedtILand).filter { !it.isNullOrEmpty() }
    return if (names.isEmpty()) null else names.joinToString(", ")
}

private fun List<Telefonnummer>.toTlfnr(): Tlfnr {
    val hoved = find { it.prioritet == 1 }
    val alternativ = find { it.prioritet == 2 }

    return Tlfnr(
        telefonHoved = hoved?.nummer,
        landskodeHoved = hoved?.landskode,
        telefonAlternativ = alternativ?.nummer,
        landskodeAlternativ = alternativ?.landskode
    )
}

private val KjoennType.beskrivelse: String
    get() = when (this) {
        KjoennType.MANN -> "Mann"
        KjoennType.KVINNE -> "Kvinne"
        else -> "Ukjent"
    }

private val Sivilstandstype.beskrivelse: String
    get() = when (this) {
        Sivilstandstype.UOPPGITT -> "Uoppgitt"
        Sivilstandstype.UGIFT -> "Ugift"
        Sivilstandstype.GIFT -> "Gift"
        Sivilstandstype.ENKE_ELLER_ENKEMANN -> "Enke/-mann"
        Sivilstandstype.SKILT -> "Skilt"
        Sivilstandstype.SEPARERT -> "Separert"
        Sivilstandstype.REGISTRERT_PARTNER -> "Registrert partner"
        Sivilstandstype.SEPARERT_PARTNER -> "Separert partner"
        Sivilstandstype.SKILT_PARTNER -> "Skilt partner"
        Sivilstandstype.GJENLEVENDE_PARTNER -> "Gjenlevende partner"
        else -> "Ukjent"
    }
