package no.nav.tms.personopplysninger.api.personalia.norg2

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.*
import io.ktor.http.isSuccess
import no.nav.tms.personopplysninger.api.common.ConsumerException
import no.nav.tms.personopplysninger.api.common.HeaderHelper.addNavHeaders

class Norg2Consumer(
    private val client: HttpClient,
    private val norg2Url: String
) {

    private val log = KotlinLogging.logger { }
    private val securelog = KotlinLogging.logger("secureLog")

    suspend fun hentEnhet(geografisk: String): Norg2Enhet? {
        val response: HttpResponse =
            client.get("$norg2Url/api/v1/enhet/navkontor/$geografisk") {
                addNavHeaders()
            }
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            val message = response.bodyAsText()

            log.warn { "Feil oppstod ved henting av enhet, returnerer tomt objekt. Status=[${response.status}]" }
            securelog.warn { "Feil oppstod ved henting av enhet, returnerer tomt objekt. Status=[${response.status}], melding=[$message]" }
            null
        }
    }

    suspend fun hentKontaktinfo(enhetsnr: String): Norg2EnhetKontaktinfo {
        val response: HttpResponse =
            client.get("$norg2Url/api/v2/enhet/$enhetsnr/kontaktinformasjon") {
                addNavHeaders()
            }
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            throw ConsumerException.fromResponse("norg2", response)
        }
    }
}
