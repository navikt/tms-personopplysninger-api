package no.nav.tms.personopplysninger.api.personalia.addresse

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.tms.personopplysninger.api.kodeverk.AdresseKodeverk
import no.nav.pdl.generated.dto.hentpersonquery.Kontaktadresse as PdlKontaktadresse

data class Kontaktadresse(
    val gyldigTilOgMed: String?,
    val coAdressenavn: String?,
    val kilde: String?,
    val adresse: Adresse
) {
    companion object {
        private val log = KotlinLogging.logger { }

        fun mapAdresse(kontaktadresse: PdlKontaktadresse, kodeverk: AdresseKodeverk): Kontaktadresse? {
            return mapEffektivAdresse(kontaktadresse, kodeverk)?.let {
                Kontaktadresse(
                    gyldigTilOgMed = kontaktadresse.gyldigTilOgMed,
                    coAdressenavn = kontaktadresse.coAdressenavn,
                    kilde = kontaktadresse.metadata.master.lowercase(),
                    adresse = it
                )
            }
        }

        private fun mapEffektivAdresse(kontaktadresse: PdlKontaktadresse, kodeverk: AdresseKodeverk): Adresse? = with(kontaktadresse) {
            return when {
                vegadresse != null -> Vegadresse.mapAdresse(vegadresse, kodeverk.poststed, kodeverk.kommune)
                postadresseIFrittFormat != null -> PostadresseIFrittFormat.mapAdresse(postadresseIFrittFormat, kodeverk.poststed)
                postboksadresse != null -> Postboksadresse.mapAdresse(postboksadresse, kodeverk.poststed)
                utenlandskAdresse != null -> UtenlandskAdresse.mapAdresse(utenlandskAdresse, kodeverk.land)
                utenlandskAdresseIFrittFormat != null -> UtenlandskAdresseIFrittFormat.mapAdresse(utenlandskAdresseIFrittFormat, kodeverk.land)
                else -> {
                    log.warn { "Forsøkte å mappe oppholdsadresse på uventet format, null returnert." }
                    null
                }
            }
        }
    }
}

val PdlKontaktadresse.postnummer: String?
    get() = when {
        vegadresse != null -> vegadresse.postnummer
        postadresseIFrittFormat != null -> postadresseIFrittFormat.postnummer
        postboksadresse != null -> postboksadresse.postnummer
        else -> null
    }

val PdlKontaktadresse.landkode: String?
    get() = when {
        utenlandskAdresse != null -> utenlandskAdresse.landkode
        utenlandskAdresseIFrittFormat != null -> utenlandskAdresseIFrittFormat.landkode
        else -> null
    }

val PdlKontaktadresse.kommunenummer: String?
    get() = when {
        vegadresse != null -> vegadresse.kommunenummer
        else -> null
    }
