package no.nav.tms.personopplysninger.api.sporingslogg

import no.nav.tms.personopplysninger.api.UserPrincipal
import no.nav.tms.personopplysninger.api.kodeverk.KodeverkConsumer
import java.time.LocalDateTime


class SporingsloggService(
    private val sporingsloggConsumer: SporingsloggConsumer,
    private val eregServicesConsumer: EregServicesConsumer,
    private val kodeverkConsumer: KodeverkConsumer
) {
    suspend fun hentSporingslogg(user: UserPrincipal): List<Sporingslogg> {
        return sporingsloggConsumer.getSporingslogg(user).map {
            Sporingslogg(
                tema = temanavn(it.tema),
                uthentingsTidspunkt = it.uthentingsTidspunkt,
                mottaker = it.mottaker,
                mottakernavn = eregServicesConsumer.hentOrganisasjonsnavn(it.mottaker),
                leverteData = it.leverteData,
                samtykkeToken = it.samtykkeToken
            )
        }
    }

    private suspend fun temanavn(temakode: String): String {
        return kodeverkConsumer.hentTema()
            .term(temakode)
    }
}

data class Sporingslogg(
    val tema: String,
    val uthentingsTidspunkt: LocalDateTime? = null,
    val mottaker: String,
    val mottakernavn: String? = null,
    val leverteData: String? = null,
    val samtykkeToken: String? = null
)
