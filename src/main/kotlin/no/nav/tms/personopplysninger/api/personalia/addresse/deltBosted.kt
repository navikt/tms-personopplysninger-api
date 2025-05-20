package no.nav.tms.personopplysninger.api.personalia.addresse

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.pdl.generated.dto.hentpersonquery.DeltBosted as PdlDeltBosted
import no.nav.tms.personopplysninger.api.kodeverk.AdresseKodeverk



data class DeltBosted(
    val coAdressenavn: String?,
    val adresse: Adresse
) {
    companion object {
        private val log = KotlinLogging.logger {}

        fun mapAdresse(deltBosted: PdlDeltBosted, kodeverk: AdresseKodeverk): DeltBosted? {
            return mapEffektivAdresse(deltBosted, kodeverk)?.let { adresse ->
                DeltBosted(
                    coAdressenavn = deltBosted.coAdressenavn,
                    adresse = adresse
                )
            }
        }

        private fun mapEffektivAdresse(
            deltBosted: PdlDeltBosted,
            kodeverk: AdresseKodeverk
        ): Adresse? = with(deltBosted) {
            when {
                vegadresse != null-> Vegadresse.mapAdresse(vegadresse, kodeverk.poststed, kodeverk.kommune)
                matrikkeladresse != null-> Matrikkeladresse.mapAdresse(matrikkeladresse, kodeverk.poststed, kodeverk.kommune)
                utenlandskAdresse != null-> UtenlandskAdresse.mapAdresse(utenlandskAdresse, kodeverk.land)
                ukjentBosted != null-> Ukjentbosted(kodeverk.kommune)
                else -> {
                    log.warn { "Forsøkte å mappe deltbosted på uventet format, null returnert." }
                    null
                }
            }
        }
    }
}


val PdlDeltBosted.postnummer: String?
    get() = when {
        vegadresse != null -> vegadresse.postnummer
        matrikkeladresse != null -> matrikkeladresse.postnummer
        else -> null
    }

val PdlDeltBosted.landkode: String?
    get() = when {
        utenlandskAdresse != null -> utenlandskAdresse.landkode
        else -> null
    }

val PdlDeltBosted.kommunenummer: String?
    get() = when {
        vegadresse != null -> vegadresse.kommunenummer
        matrikkeladresse != null -> matrikkeladresse.kommunenummer
        ukjentBosted != null -> ukjentBosted.bostedskommune
        else -> null
    }

