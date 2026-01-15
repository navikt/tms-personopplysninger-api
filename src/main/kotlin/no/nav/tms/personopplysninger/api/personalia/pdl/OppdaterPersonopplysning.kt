package no.nav.tms.personopplysninger.api.personalia.pdl

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.tms.personopplysninger.api.personalia.TelefonnummerEndring


data class OppdaterPersonopplysning(
    val ident: String,
    val opplysningstype: Opplysningstype,
    val endringstype: EndringsType,
    val endringsmelding: Endringsmelding,
    val opplysningsId: String? = null
) {
    companion object {

        fun endreTelefonnummerPayload(ident: String, endringsmelding: TelefonnummerEndring): OppdaterPersonopplysning {
            return OppdaterPersonopplysning(
                ident = ident,
                opplysningstype = Opplysningstype.TELEFONNUMMER,
                endringstype = EndringsType.OPPRETT,
                endringsmelding = Telefonnummer(
                    landskode = endringsmelding.landskode,
                    nummer = endringsmelding.nummer,
                    prioritet = endringsmelding.prioritet,
                )
            )
        }

        fun slettTelefonnummerPayload(ident: String, opplysningsId: String): OppdaterPersonopplysning {
            return OppdaterPersonopplysning(
                ident = ident,
                opplysningstype = Opplysningstype.TELEFONNUMMER,
                endringstype = EndringsType.OPPHOER,
                endringsmelding = OpphoerEndringsMelding(),
                opplysningsId = opplysningsId
            )
        }

        fun slettKontaktadressePayload(ident: String, opplysningsId: String): OppdaterPersonopplysning {
            return OppdaterPersonopplysning(
                ident = ident,
                opplysningstype = Opplysningstype.KONTAKTADRESSE,
                endringstype = EndringsType.OPPHOER,
                endringsmelding = OpphoerEndringsMelding(),
                opplysningsId = opplysningsId
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

enum class Opplysningstype {
    TELEFONNUMMER, KONTAKTADRESSE
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
