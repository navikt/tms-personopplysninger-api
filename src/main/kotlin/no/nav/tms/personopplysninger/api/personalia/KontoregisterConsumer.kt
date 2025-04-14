package no.nav.tms.personopplysninger.api.personalia

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import no.nav.tms.personopplysninger.api.common.HeaderHelper
import no.nav.tms.personopplysninger.api.common.HeaderHelper.addNavHeaders
import no.nav.tms.personopplysninger.api.common.HeaderHelper.authorization
import no.nav.tms.personopplysninger.api.common.TokenExchanger
import no.nav.tms.token.support.tokenx.validation.user.TokenXUser
import java.util.*

class KontoregisterConsumer(
    private val client: HttpClient,
    private val kontoregisterUrl: String,
    private val tokenExchanger: TokenExchanger,
) {
    private val logger = KotlinLogging.logger { }

    suspend fun hentAktivKonto(user: TokenXUser): Konto? {
        val accessToken = tokenExchanger.kontoregisterToken(user.tokenString)
        val request = HentAktivKonto(user.ident)

        try {
            val response: HttpResponse =
                client.post("$kontoregisterUrl/api/borger/v1/hent-aktiv-konto") {
                    addNavHeaders()
                    authorization(accessToken)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            return if (response.status.isSuccess()) {
                response.body()
            } else if (response.status == HttpStatusCode.NotFound) {
                logger.info { "Kall mot kontoregister gav tomt svar. Returnerer null." }
                null
            } else {
                logger.warn { "Kall mot kontoregister feilet med status ${response.status}. Returnerer feilobjekt." }
                Konto(error = true)
            }
        } catch (e: Exception) {
            when (e) {
                is SocketTimeoutException,
                is HttpRequestTimeoutException,
                is ConnectTimeoutException -> logger.warn { "Kall mot kontoregister timet ut. Returnerer feilobjekt." }

                else -> logger.warn { "Ukjent feil ved kall mot kontoregister. Returnerer feilobjekt." }
            }
            return Konto(error = true)
        }
    }

    suspend fun hentLandkoder(): List<KontoResponse.Landkode> {
        return client.get("$kontoregisterUrl/api/system/v1/hent-landkoder").body()
    }

    suspend fun hentValutakoder(): List<KontoResponse.Valutakode> {
        return client.get("$kontoregisterUrl/api/system/v1/hent-valutakoder").body()
    }
}
