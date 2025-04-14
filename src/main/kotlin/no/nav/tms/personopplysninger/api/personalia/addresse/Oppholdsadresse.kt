package no.nav.tms.personopplysninger.api.personalia.addresse

import no.nav.tms.personopplysninger.api.personalia.AdresseKodeverk
import no.nav.tms.personopplysninger.api.personalia.addresse.AdresseMappingType.*
import no.nav.pdl.generated.dto.hentpersonquery.Oppholdsadresse as PdlOppholdsadresse

data class Oppholdsadresse(
    val gyldigTilOgMed: String?,
    val coAdressenavn: String?,
    val kilde: String?,
    val adresse: Adresse?
) {
    companion object {
        fun fromResult(oppholdsadresse: PdlOppholdsadresse, kodeverk: AdresseKodeverk): Oppholdsadresse? {
            val adresse = oppholdsadresse.transformAdresse(kodeverk)

            if (adresse == null && oppholdAnnetSted == null) return null

            return Oppholdsadresse(
                gyldigTilOgMed = gyldigTilOgMed,
                coAdressenavn = coAdressenavn,
                kilde = metadata.master.lowercase(),
                adresse = adresse
            )
        }
    }
}

fun PdlOppholdsadresse.toOutbound(kodeverk: AdresseKodeverk): Oppholdsadresse? {
    val adresse = this.transformAdresse(kodeverk)

    if (adresse == null && oppholdAnnetSted == null) return null

    return Oppholdsadresse(
        gyldigTilOgMed = gyldigTilOgMed,
        coAdressenavn = coAdressenavn,
        kilde = metadata.master.lowercase(),
        adresse = adresse
    )
}

private fun PdlOppholdsadresse.transformAdresse(oppholdsaddresse: PdlOppholdsadresse, kodeverk: AdresseKodeverk): Adresse? {
    return when (mappingType) {
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
}

val PdlOppholdsadresse.mappingType: AdresseMappingType
    get() = if (vegadresse != null) {
        INNLAND_VEGADRESSE
    } else if (matrikkeladresse != null) {
        MATRIKKELADRESSE
    } else if (utenlandskAdresse != null) {
        UTLAND_ADRESSE
    } else {
        AdresseMappingType.EMPTY
    }

val PdlOppholdsadresse.postnummer: String?
    get() = when (mappingType) {
        INNLAND_VEGADRESSE -> vegadresse?.postnummer
        MATRIKKELADRESSE -> matrikkeladresse?.postnummer
        else -> null
    }

val PdlOppholdsadresse.landkode: String?
    get() = when (mappingType) {
        UTLAND_ADRESSE -> utenlandskAdresse?.landkode
        else -> null
    }

val PdlOppholdsadresse.kommunenummer: String?
    get() = when (mappingType) {
        INNLAND_VEGADRESSE -> vegadresse?.kommunenummer
        MATRIKKELADRESSE -> matrikkeladresse?.kommunenummer
        else -> null
    }
