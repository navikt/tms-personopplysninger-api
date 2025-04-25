package no.nav.tms.personopplysninger.api.personalia.addresse

import no.nav.pdl.generated.dto.hentpersonquery.Person
import no.nav.tms.personopplysninger.api.kodeverk.PersonaliaKodeverk

data class Adresser(
    val kontaktadresser: List<Kontaktadresse> = emptyList(),
    val bostedsadresse: Bostedsadresse? = null,
    val oppholdsadresser: List<Oppholdsadresse> = emptyList(),
    val deltBosted: DeltBosted? = null,
) {
    companion object {
        fun mapAdresser(person: Person, kodeverk: PersonaliaKodeverk): Adresser {
            return Adresser(
                kontaktadresser = person.kontaktadresse
                    .zip(kodeverk.kontaktadresseKodeverk, Kontaktadresse::mapAdresse)
                    .filterNotNull(),
                bostedsadresse = person.bostedsadresse.firstOrNull()
                    ?.let { Bostedsadresse.mapAdresse(it, kodeverk.bostedsadresseKodeverk) },
                oppholdsadresser = person.oppholdsadresse
                    .zip(kodeverk.oppholdsadresseKodeverk, Oppholdsadresse::mapAdresse)
                    .filterNotNull(),
                deltBosted = person.deltBosted.firstOrNull()
                    ?.let { DeltBosted.mapAdresse(it, kodeverk.deltBostedKodeverk) }
            )
        }
    }
}

interface Adresse {
    val type: Adressetype
}

enum class Adressetype {
    VEGADRESSE, POSTADRESSE_I_FRITT_FORMAT, POSTBOKSADRESSE, UTENLANDSK_ADRESSE, UTENLANDSK_ADRESSE_I_FRITT_FORMAT, UKJENTBOSTED, MATRIKKELADRESSE
}

data class Ukjentbosted(
    val bostedskommune: String?
) : Adresse {
    override val type: Adressetype get() = Adressetype.UKJENTBOSTED
}
