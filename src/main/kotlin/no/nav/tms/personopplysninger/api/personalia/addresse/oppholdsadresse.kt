package no.nav.tms.personopplysninger.api.personalia.addresse

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.tms.personopplysninger.api.kodeverk.AdresseKodeverk
import no.nav.pdl.generated.dto.hentpersonquery.Oppholdsadresse as PdlOppholdsadresse

data class Oppholdsadresse(
    val gyldigTilOgMed: String?,
    val coAdressenavn: String?,
    val kilde: String?,
    val adresse: Adresse?
) {
    companion object {
        fun mapAdresse(oppholdsadresse: PdlOppholdsadresse, kodeverk: AdresseKodeverk): Oppholdsadresse? {
            val adresse = no.nav.tms.personopplysninger.api.personalia.addresse.mapAdresse(oppholdsadresse, kodeverk)

            if (adresse == null && oppholdsadresse.oppholdAnnetSted == null) {
                return null
            }

            return Oppholdsadresse(
                gyldigTilOgMed = oppholdsadresse.gyldigTilOgMed,
                coAdressenavn = oppholdsadresse.coAdressenavn,
                kilde = oppholdsadresse.metadata.master.lowercase(),
                adresse = adresse
            )
        }
    }
}

private val log = KotlinLogging.logger { }

private fun mapAdresse(oppholdsadresse: PdlOppholdsadresse, kodeverk: AdresseKodeverk): Adresse? {
    return when {
        oppholdsadresse.vegadresse != null -> Vegadresse.mapAdresse(oppholdsadresse.vegadresse, kodeverk.poststed, kodeverk.kommune)
        oppholdsadresse.matrikkeladresse != null -> Matrikkeladresse.mapAdresse(oppholdsadresse.matrikkeladresse, kodeverk.poststed, kodeverk.kommune)
        oppholdsadresse.utenlandskAdresse != null -> UtenlandskAdresse.mapAdresse(oppholdsadresse.utenlandskAdresse, kodeverk.land)
        else -> {
            // Adresse kan være null dersom oppholdAnnetSted er satt. Da trenger vi ikke logge warning.
            if (oppholdsadresse.oppholdAnnetSted == null) {
                log.warn { "Forsøkte å mappe oppholdsadresse på uventet format, null returnert." }
            }
            null
        }
    }
}

val PdlOppholdsadresse.postnummer: String?
    get() = when  {
        vegadresse != null -> vegadresse.postnummer
        matrikkeladresse != null -> matrikkeladresse.postnummer
        else -> null
    }

val PdlOppholdsadresse.landkode: String?
    get() = when {
        utenlandskAdresse != null -> utenlandskAdresse.landkode
        else -> null
    }

val PdlOppholdsadresse.kommunenummer: String?
    get() = when  {
        vegadresse != null -> vegadresse.kommunenummer
        matrikkeladresse != null -> matrikkeladresse.kommunenummer
        else -> null
    }
