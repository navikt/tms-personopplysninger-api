package no.nav.tms.personopplysninger.api.personalia.norg2

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import no.nav.tms.personopplysninger.api.common.HeaderHelper
import no.nav.tms.personopplysninger.api.common.HeaderHelper.addNavHeaders
import no.nav.tms.personopplysninger.api.personalia.norg2.dto.Norg2Enhet
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class Norg2Consumer(
    private val client: HttpClient,
    private val norg2Url: String
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    suspend fun hentEnhet(token: String, geografisk: String): Norg2Enhet? {
        val response: HttpResponse =
            client.get("$norg2Url/api/v1/enhet/navkontor/$geografisk") {
                addNavHeaders()
            }
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            logger.warn("Feil oppstod ved henting av enhet, returnerer tomt objekt. Status=[${response.status}], melding=[${response.body<String>()}]")
            null
        }
    }

    suspend fun hentKontaktinfo(token: String, enhetsnr: String): Norg2EnhetKontaktinfo {
        val endpoint = environment.norg2Url.plus("/api/v2/enhet/$enhetsnr/kontaktinformasjon")

        val response: HttpResponse =
            client.get("endpoint") {
                header(HEADER_NAV_CALL_ID, UUID.randomUUID())
                header(HEADER_NAV_CONSUMER_ID, CONSUMER_ID)
                header(HEADER_NAV_CONSUMER_TOKEN, token)
                header("enhetsnr", enhetsnr)
            }
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            throw RuntimeException(consumerErrorMessage(endpoint, response.status.value, response.body()))
        }
    }
}
