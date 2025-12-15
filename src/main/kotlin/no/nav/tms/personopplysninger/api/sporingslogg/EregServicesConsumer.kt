package no.nav.tms.personopplysninger.api.sporingslogg

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.*
import io.ktor.http.isSuccess
import no.nav.tms.common.logging.TeamLogs
import no.nav.tms.personopplysninger.api.common.ConsumerMetrics


class EregServicesConsumer(
    private val client: HttpClient,
    private val eregServicesUrl: String
) {
    private val log = KotlinLogging.logger { }
    private val teamLog = TeamLogs.logger { }

    private val metrics = ConsumerMetrics.init { }

    suspend fun hentOrganisasjonsnavn(orgnr: String): String {

        val eregResponse = metrics.measureRequest("organisasjon_info") {
            client.get("$eregServicesUrl/v1/organisasjon/$orgnr/noekkelinfo")
        }

        return if (eregResponse.status.isSuccess()) {
            eregResponse.body<EregOrganisasjon>()
                .navn
                .joinedToString()
        } else {
            val feilmelding = eregResponse.bodyAsText()
            teamLog.warn { "Oppslag mot EREG på organisasjonsnummer [$orgnr] feilet med melding: [$feilmelding]." }
            log.warn { "Oppslag mot EREG på organisasjonsnummer [$orgnr] feilet." }

            orgnr
        }
    }

    data class EregOrganisasjon(
        val navn: Navn
    )

    data class Navn(
        val navnelinje1: String? = null,
        val navnelinje2: String? = null,
        val navnelinje3: String? = null,
        val navnelinje4: String? = null,
        val navnelinje5: String? = null
    ) {
        fun joinedToString(): String {
            return listOfNotNull(navnelinje1, navnelinje2, navnelinje3, navnelinje4, navnelinje5).joinToString(" ")
        }
    }
}
