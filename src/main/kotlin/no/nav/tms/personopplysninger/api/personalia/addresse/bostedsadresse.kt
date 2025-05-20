package no.nav.tms.personopplysninger.api.personalia.addresse

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.tms.personopplysninger.api.kodeverk.AdresseKodeverk
import no.nav.pdl.generated.dto.hentpersonquery.Bostedsadresse as PdlBostedsadresse

data class Bostedsadresse(
    val angittFlyttedato: String? = null,
    val coAdressenavn: String?,
    val adresse: Adresse
) {
    companion object {

        private val log = KotlinLogging.logger {}

        fun mapAdresse(bostedsadresse: PdlBostedsadresse, kodeverk: AdresseKodeverk): Bostedsadresse? {
            return mapEffektivAdresse(bostedsadresse, kodeverk)?.let { adresse ->
                Bostedsadresse(
                    angittFlyttedato = bostedsadresse.angittFlyttedato,
                    coAdressenavn = bostedsadresse.coAdressenavn,
                    adresse = adresse
                )
            }
        }

        private fun mapEffektivAdresse(
            bostedsadresse: PdlBostedsadresse,
            kodeverk: AdresseKodeverk
        ): Adresse? = with(bostedsadresse) {
            when {
                vegadresse != null -> Vegadresse.mapAdresse(vegadresse, kodeverk.poststed, kodeverk.kommune)
                matrikkeladresse != null -> Matrikkeladresse.mapAdresse(matrikkeladresse, kodeverk.poststed, kodeverk.kommune)
                utenlandskAdresse != null-> UtenlandskAdresse.mapAdresse(utenlandskAdresse, kodeverk.land)
                ukjentBosted != null -> Ukjentbosted(kodeverk.kommune)
                else -> {
                    log.warn { "Forsøkte å mappe bostedsadresse på uventet format, null returnert." }
                    null
                }
            }
        }
    }
}

val PdlBostedsadresse.postnummer: String?
    get() = when {
        vegadresse != null -> vegadresse.postnummer
        matrikkeladresse != null -> matrikkeladresse.postnummer
        else -> null
    }

val PdlBostedsadresse.landkode: String?
    get() = when {
        utenlandskAdresse != null-> utenlandskAdresse.landkode
        else -> null
    }

val PdlBostedsadresse.kommunenummer: String?
    get() = when {
        vegadresse != null -> vegadresse.kommunenummer
        matrikkeladresse != null -> matrikkeladresse.kommunenummer
        ukjentBosted != null -> ukjentBosted.bostedskommune
        else -> null
    }
