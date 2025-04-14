package no.nav.tms.personopplysninger.api.personalia.addresse

data class Adresser(
    val kontaktadresser: List<Kontaktadresse> = emptyList(),
    val bostedsadresse: Bostedsadresse? = null,
    val oppholdsadresser: List<Oppholdsadresse> = emptyList(),
    val deltBosted: DeltBosted? = null,
)

interface Adresse {
    val type: Adressetype
}

enum class Adressetype {
    VEGADRESSE, POSTADRESSE_I_FRITT_FORMAT, POSTBOKSADRESSE, UTENLANDSK_ADRESSE, UTENLANDSK_ADRESSE_I_FRITT_FORMAT, UKJENTBOSTED, MATRIKKELADRESSE
}

enum class AdresseMappingType {
    INNLAND_VEGADRESSE, INNLAND_POSTBOKSADRESSE, INNLAND_FRIFORMADRESSE, UTLAND_ADRESSE, UTLAND_FRIFORMADRESSE, MATRIKKELADRESSE, UKJENT_BOSTED, EMPTY
}

data class Bostedsadresse(
    val angittFlyttedato: String? = null,
    val coAdressenavn: String?,
    val adresse: Adresse
)

data class DeltBosted(
    val coAdressenavn: String?,
    val adresse: Adresse
)

data class Kontaktadresse(
    val gyldigTilOgMed: String?,
    val coAdressenavn: String?,
    val kilde: String?,
    val adresse: Adresse
)

data class Matrikkeladresse(
    val bruksenhetsnummer: String?,
    val tilleggsnavn: String?,
    val postnummer: String?,
    val poststed: String?,
    val kommune: String?,
) : Adresse {
    override val type: Adressetype get() = Adressetype.MATRIKKELADRESSE
}

data class PostadresseIFrittFormat(
    val adresselinje1: String?,
    val adresselinje2: String?,
    val adresselinje3: String?,
    val postnummer: String?,
    val poststed: String?
) : Adresse {
    override val type: Adressetype get() = Adressetype.POSTADRESSE_I_FRITT_FORMAT
}

data class Postboksadresse(
    val postbokseier: String?,
    val postboks: String,
    val postnummer: String?,
    val poststed: String?
) : Adresse {
    override val type: Adressetype get() = Adressetype.POSTBOKSADRESSE
}

data class Ukjentbosted(
    val bostedskommune: String?
) : Adresse {
    override val type: Adressetype get() = Adressetype.UKJENTBOSTED
}

data class UtenlandskAdresse(
    val adressenavnNummer: String?,
    val bygningEtasjeLeilighet: String?,
    val postboksNummerNavn: String?,
    val postkode: String?,
    val bySted: String?,
    val regionDistriktOmraade: String?,
    val land: String?
) : Adresse {
    override val type: Adressetype get() = Adressetype.UTENLANDSK_ADRESSE
}

data class UtenlandskAdresseIFrittFormat(
    val adresselinje1: String?,
    val adresselinje2: String?,
    val adresselinje3: String?,
    val land: String?
) : Adresse {
    override val type: Adressetype get() = Adressetype.UTENLANDSK_ADRESSE_I_FRITT_FORMAT
}

data class Vegadresse(
    val husnummer: String?,
    val husbokstav: String?,
    val bruksenhetsnummer: String?,
    val adressenavn: String?,
    val kommune: String?,
    val tilleggsnavn: String?,
    val postnummer: String?,
    val poststed: String?
) : Adresse {
    override val type: Adressetype get() = Adressetype.VEGADRESSE
}
