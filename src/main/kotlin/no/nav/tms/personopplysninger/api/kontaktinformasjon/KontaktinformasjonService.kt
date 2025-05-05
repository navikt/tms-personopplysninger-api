package no.nav.tms.personopplysninger.api.kontaktinformasjon

import no.nav.tms.personopplysninger.api.UserPrincipal
import no.nav.tms.personopplysninger.api.kodeverk.KodeverkConsumer

class KontaktinformasjonService(
    private val kontaktinfoConsumer: KontaktinfoConsumer,
    private val kodeverkConsumer: KodeverkConsumer,
) {
    suspend fun hentKontaktinformasjon(user: UserPrincipal): Kontaktinformasjon {
        val inbound = kontaktinfoConsumer.hentKontaktinformasjon(user)
        val spraakTerm = inbound.spraak?.uppercase()?.let { kodeverkConsumer.hentSpraak().term(it) }

        return Kontaktinformasjon.fromDigitalInfo(inbound, spraakTerm)
    }
}

