package no.nav.tms.personopplysninger.api.personalia.pdl

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.tms.personopplysninger.api.personalia.TelefonnummerEndring


data class EndrePersonopplysning(
    val ident: String,
    val endringstype: EndringsType,
    val opplysningstype: String,
    val endringsmelding: Endringsmelding,
    val opplysningsId: String? = null
) {
    companion object {
        private const val TELEFONNUMMER = "TELEFONNUMMER"
        private const val KONTAKTADRESSE = "KONTAKTADRESSE"

        fun slettTelefonnummerPayload(ident: String, opplysningsId: String): EndrePersonopplysning {
            return EndrePersonopplysning(
                ident = ident,
                endringstype = EndringsType.OPPHOER,
                opplysningstype = TELEFONNUMMER,
                endringsmelding = OpphoerEndringsMelding(),
                opplysningsId = opplysningsId
            )
        }

        fun endreTelefonnummerPayload(ident: String, endringsmelding: TelefonnummerEndring): EndrePersonopplysning {
            return EndrePersonopplysning(
                ident = ident,
                endringstype = EndringsType.OPPRETT,
                opplysningstype = TELEFONNUMMER,
                endringsmelding = Telefonnummer(
                    landskode = endringsmelding.landskode,
                    nummer = endringsmelding.nummer,
                    prioritet = endringsmelding.prioritet,
                )
            )
        }

        fun slettKontaktadressePayload(ident: String, opplysningsId: String): EndrePersonopplysning {
            return EndrePersonopplysning(
                ident = ident,
                endringstype = EndringsType.OPPHOER,
                opplysningstype = KONTAKTADRESSE,
                endringsmelding = OpphoerEndringsMelding(),
                opplysningsId = opplysningsId
            )
        }
    }
}

data class Telefonnummer(
    @JsonProperty("@type")
    override val subtype: String = "TELEFONNUMMER",
    override val kilde: String = "BRUKER SELV",
    val landskode: String,
    val nummer: String,
    val prioritet: Int? = 1,
): Endringsmelding

interface Endringsmelding {
    val subtype: String
    val kilde: String
}

enum class EndringsType {
    OPPHOER, OPPRETT
}

data class OpphoerEndringsMelding(
    @JsonProperty("@type")
    override val subtype: String = "OPPHOER",
    override val kilde: String = "BRUKER SELV",
) : Endringsmelding
