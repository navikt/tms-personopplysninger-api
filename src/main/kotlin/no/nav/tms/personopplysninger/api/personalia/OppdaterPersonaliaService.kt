package no.nav.tms.personopplysninger.api.personalia

import io.prometheus.metrics.core.metrics.Counter
import no.nav.pdl.generated.dto.HentKontaktadresseQuery
import no.nav.pdl.generated.dto.HentTelefonQuery
import no.nav.tms.personopplysninger.api.personalia.pdl.PdlApiConsumer
import no.nav.tms.personopplysninger.api.personalia.pdl.PdlMottakConsumer
import no.nav.tms.token.support.user.token.verification.UserPrincipal
import java.lang.IllegalArgumentException

class OppdaterPersonaliaService(
    private val pdlApiConsumer: PdlApiConsumer,
    private val pdlMottakConsumer: PdlMottakConsumer
) {

    private val PDL_MASTER = "PDL"

    suspend fun endreTelefonnummer(user: UserPrincipal, telefonnummer: TelefonnummerEndring): EndringResult {
        if (!setOf(1, 2).contains(telefonnummer.prioritet)) {
            throw RuntimeException("Støtter kun prioritet [1, 2] eller type ['HJEM', 'MOBIL']")
        } else {
            return pdlMottakConsumer.endreTelefonnummer(user, telefonnummer)
                .also(EndrePersonaliaMetrics::telefonnummerEndret)
        }
    }

    suspend fun slettTelefonNummer(user: UserPrincipal, telefonnummer: TelefonnummerEndring): EndringResult {
        return pdlApiConsumer.hentTelefon(user)
            .let { getOpplysningsId(it, telefonnummer.landskode, telefonnummer.nummer)}
            .let { pdlMottakConsumer.slettTelefonnummer(user, it) }
            .also(EndrePersonaliaMetrics::telefonnummerSlettet)
    }

    suspend fun slettPdlKontaktadresse(user: UserPrincipal): EndringResult {

        return pdlApiConsumer.hentKontaktadresse(user)
            .let { getOpplysningsId(it, master = PDL_MASTER) }
            .let { pdlMottakConsumer.slettKontaktadresse(user, it) }
            .also(EndrePersonaliaMetrics::kontaktadresseSlettet)
    }

    private fun getOpplysningsId(result: HentTelefonQuery.Result, landskode: String, telefonnummer: String): String {
        return result.person?.telefonnummer
            ?.find { it.landskode == landskode && it.nummer == telefonnummer }
            ?.metadata
            ?.opplysningsId
            ?: throw SlettPersonopplysningException("Fant ikke opplysningsId for angitt telefonnummer")
    }

    private fun getOpplysningsId(result: HentKontaktadresseQuery.Result, master: String): String {
        return result.person?.kontaktadresse
            ?.firstOrNull { it.metadata.master.equals(master, ignoreCase = true) }
            ?.metadata
            ?.opplysningsId
            ?: throw SlettPersonopplysningException("Fant ikke opplysningsId for kontaktadresse der $master er master.")
    }
}

class SlettPersonopplysningException(message: String): IllegalArgumentException(message)

private object EndrePersonaliaMetrics {
    private const val NAMESPACE = "personalia_api"

    private val telefonnummerEndret = Counter.builder()
        .name("${NAMESPACE}_telefonnummer_endret")
        .help("Antall ganger telefonnummer ble endret")
        .labelNames("result")
        .register()

    private val telefonnummerSlettet = Counter.builder()
        .name("${NAMESPACE}_telefonnummer_slettet")
        .help("Antall ganger telefonnummer ble slettet")
        .labelNames("result")
        .register()

    private val kontaktadresseSlettet = Counter.builder()
        .name("${NAMESPACE}_kontaktadresse_slettet")
        .help("Antall ganger kontaktadresse ble slettet")
        .labelNames("result")
        .register()

    fun telefonnummerEndret(result: EndringResult) {
        telefonnummerEndret
            .labelValues(result.statusType.lowercase())
            .inc()
    }

    fun telefonnummerSlettet(result: EndringResult) {
        telefonnummerSlettet
            .labelValues(result.statusType.lowercase())
            .inc()
    }

    fun kontaktadresseSlettet(result: EndringResult) {
        kontaktadresseSlettet
            .labelValues(result.statusType.lowercase())
            .inc()
    }
}
