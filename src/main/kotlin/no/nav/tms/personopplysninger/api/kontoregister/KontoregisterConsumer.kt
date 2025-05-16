package no.nav.tms.personopplysninger.api.kontoregister

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import no.nav.tms.personopplysninger.api.UserPrincipal
import no.nav.tms.personopplysninger.api.common.ConsumerMetrics
import no.nav.tms.personopplysninger.api.common.SingletonCache
import no.nav.tms.personopplysninger.api.common.HeaderHelper.addNavHeaders
import no.nav.tms.personopplysninger.api.common.HeaderHelper.authorization
import no.nav.tms.personopplysninger.api.common.TokenExchanger
import java.time.Duration

class KontoregisterConsumer(
    private val client: HttpClient,
    private val kontoregisterUrl: String,
    private val tokenExchanger: TokenExchanger,
    cacheDuration: Duration = Duration.ofMinutes(45)
) {
    private val log = KotlinLogging.logger { }

    private val landkoder = SingletonCache<List<KontoResponse.Landkode>>(expireAfter = cacheDuration)
    private val valutakoder = SingletonCache<List<KontoResponse.Valutakode>>(expireAfter = cacheDuration)

    private val metrics = ConsumerMetrics.init { }

    suspend fun hentAktivKonto(user: UserPrincipal): Konto? {
        val accessToken = tokenExchanger.kontoregisterToken(user.accessToken)
        val request = HentAktivKonto(user.ident)

        try {
            val response = metrics.measureRequest("aktiv_konto") {
                client.post("$kontoregisterUrl/api/borger/v1/hent-aktiv-konto") {
                    addNavHeaders()
                    authorization(accessToken)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            }

            return if (response.status.isSuccess()) {
                response.body()
            } else if (response.status == HttpStatusCode.NotFound) {
                log.info { "Kall mot kontoregister gav tomt svar. Returnerer null." }
                null
            } else {
                log.warn { "Kall mot kontoregister feilet med status ${response.status}. Returnerer feilobjekt." }
                Konto(error = true)
            }
        } catch (e: Exception) {
            when (e) {
                is SocketTimeoutException,
                is HttpRequestTimeoutException,
                is ConnectTimeoutException -> log.warn { "Kall mot kontoregister timet ut. Returnerer feilobjekt." }

                else -> log.warn { "Ukjent feil ved kall mot kontoregister. Returnerer feilobjekt." }
            }
            return Konto(error = true)
        }
    }

    suspend fun hentLandkoder(): List<KontoResponse.Landkode> {
        return landkoder.get {
            metrics.measureRequest("landkoder") {
                client.get("$kontoregisterUrl/api/system/v1/hent-landkoder").body()
            }
        }
    }

    suspend fun hentValutakoder(): List<KontoResponse.Valutakode> {
        return valutakoder.get {
            metrics.measureRequest("valutakoder") {
                client.get("$kontoregisterUrl/api/system/v1/hent-valutakoder").body()
            }
        }
    }
}
