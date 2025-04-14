package no.nav.tms.personopplysninger.api.personalia

import no.nav.pdl.generated.dto.HentPersonQuery
import no.nav.pdl.generated.dto.enums.KjoennType
import no.nav.pdl.generated.dto.enums.Sivilstandstype
import no.nav.pdl.generated.dto.hentpersonquery.Navn
import no.nav.pdl.generated.dto.hentpersonquery.Person
import no.nav.pdl.generated.dto.hentpersonquery.Telefonnummer
import no.nav.tms.personopplysninger.api.personalia.addresse.Adresser
import no.nav.tms.personopplysninger.api.personalia.addresse.Kontaktadresse
import no.nav.tms.personopplysninger.api.personalia.norg2.dto.Norg2EnhetKontaktinfo
import no.nav.pdl.generated.dto.hentpersonquery.Oppholdsadresse as PdlOppholdsaddresse

data class PersonaliaOgAdresser(
    val personalia: Personalia,
    val adresser: Adresser?,
    val enhetKontaktInformasjon: Norg2EnhetKontaktinfo?
) {
    companion object {
        fun fromResult(
            result: HentPersonQuery.Result,
            konto: Konto?,
            kodeverk: PersonaliaKodeverk,
            enhetKontaktInformasjon: Norg2EnhetKontaktinfo?
        ): PersonaliaOgAdresser {

        }
    }
}

private fun mapPersonalia(
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

private fun mapAddresser(person: Person, kodeverk: PersonaliaKodeverk): Adresser {
    return Adresser(
        kontaktadresser = mapAdresseWithKodeverk(
            adresser = person.kontaktadresse,
            kodeverk = kodeverk.kontaktadresseKodeverk,
            mapper = Kontaktadresse::toOutbound
        ),
        bostedsadresse = bostedsadresse.firstOrNull()?.toOutbound(bostedsadresseKodeverk),
        oppholdsadresser = mapAdresseWithKodeverk(
            adresser = oppholdsadresse,
            kodeverk = oppholdsadresseKodeverk,
            mapper = Oppholdsadresse::toOutbound
        ),
        deltBosted = deltBosted.firstOrNull()?.toOutbound(deltBostedKodeverk),
    )
}

private fun mapOppholdsaddresse(pdlOppholdsadresse: PdlOppholdsaddresse, kodeverk: AdresseKodeverk): Oppholdsadresse? {
    val adresse = when ([mappingType]) {
        INNLAND_VEGADRESSE -> requireNotNull(vegadresse).toOutbound(kodeverk.poststed, kodeverk.kommune)
        MATRIKKELADRESSE -> requireNotNull(matrikkeladresse).toOutbound(kodeverk.poststed, kodeverk.kommune)
        UTLAND_ADRESSE -> requireNotNull(utenlandskAdresse).toOutbound(kodeverk.land)
        else -> {
            // Adresse kan være null dersom oppholdAnnetSted er satt. Da trenger vi ikke logge warning.
            if (oppholdAnnetSted == null) {
                logger.warn("Forsøkte å mappe oppholdsadresse på uventet format, null returnert. Adressetype: $mappingType")
            }
            null
        }
    }

    if (adresse == null && oppholdAnnetSted == null) return null

    return Oppholdsadresse(
        gyldigTilOgMed = gyldigTilOgMed,
        coAdressenavn = coAdressenavn,
        kilde = metadata.master.lowercase(),
        adresse = adresse
    )
}

private fun <T, S> mapAdresseWithKodeverk(
    adresser: List<T>,
    kodeverk: List<AdresseKodeverk>,
    mapper: (T, AdresseKodeverk) -> S?
): List<S> {
    return adresser.zip(kodeverk) { a, k -> mapper(a, k) }.filterNotNull()
}
