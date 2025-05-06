package no.nav.tms.personopplysninger.api.personalia

import no.nav.pdl.generated.dto.HentKontaktadresseQuery
import no.nav.pdl.generated.dto.HentTelefonQuery
import no.nav.tms.personopplysninger.api.UserPrincipal
import no.nav.tms.personopplysninger.api.personalia.pdl.PdlApiConsumer
import no.nav.tms.personopplysninger.api.personalia.pdl.PdlMottakConsumer

class OppdaterPersonaliaService(
    private val pdlApiConsumer: PdlApiConsumer,
    private val pdlMottakConsumer: PdlMottakConsumer
) {

    suspend fun endreTelefonnummer(user: UserPrincipal, telefonnummer: TelefonnummerEndring): Endring {
        if (!setOf(1, 2).contains(telefonnummer.prioritet)) {
            throw RuntimeException("Støtter kun prioritet [1, 2] eller type ['HJEM', 'MOBIL']")
        } else {
            return pdlMottakConsumer.endreTelefonnummer(user, telefonnummer)
        }
    }

    suspend fun slettTelefonNummer(user: UserPrincipal, telefonnummer: TelefonnummerEndring): Endring {
        return pdlApiConsumer.hentTelefon(user)
            .let { findOpplysningsId(it, telefonnummer.landskode, telefonnummer.nummer)}
            .let { pdlMottakConsumer.slettTelefonnummer(user, it) }
    }
}

private fun findOpplysningsId(result: HentTelefonQuery.Result, landskode: String, telefonnummer: String): String {
    return result.person?.telefonnummer
        ?.find { it.landskode == landskode && it.nummer == telefonnummer }
        ?.metadata
        ?.opplysningsId
        ?: throw RuntimeException("Fant ikke opplysningsId for telefonnummer")
}
