package no.nav.tms.personopplysninger.api.personalia.pdl

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.tms.personopplysninger.api.personalia.TelefonnummerEndring


data class OppdaterTelefonnummer(
    val ident: String,
    val endringstype: EndringsType,
    val endringsmelding: Endringsmelding,
    val opplysningsId: String? = null
) {
    val opplysningstype: String = "TELEFONNUMMER"

    companion object {
        fun slettTelefonnummerPayload(ident: String, opplysningsId: String): OppdaterTelefonnummer {
            return OppdaterTelefonnummer(
                ident = ident,
                endringstype = EndringsType.OPPHOER,
                endringsmelding = OpphoerEndringsMelding(),
                opplysningsId = opplysningsId
            )
        }

        fun endreTelefonnummerPayload(ident: String, endringsmelding: TelefonnummerEndring): OppdaterTelefonnummer {
            return OppdaterTelefonnummer(
                ident = ident,
                endringstype = EndringsType.OPPRETT,
                endringsmelding = Telefonnummer(
                    landskode = endringsmelding.landskode,
                    nummer = endringsmelding.nummer,
                    prioritet = endringsmelding.prioritet,
                )
            )
        }
    }
}

interface Endringsmelding {
    val subtype: String
    val kilde: String
}

enum class EndringsType {
    OPPHOER, OPPRETT
}

data class Telefonnummer(
    @param:JsonProperty("@type")
    override val subtype: String = "TELEFONNUMMER",
    override val kilde: String = "BRUKER SELV",
    val landskode: String,
    val nummer: String,
    val prioritet: Int? = 1,
): Endringsmelding

data class OpphoerEndringsMelding(
    @param:JsonProperty("@type")
    override val subtype: String = "OPPHOER",
    override val kilde: String = "BRUKER SELV",
) : Endringsmelding
