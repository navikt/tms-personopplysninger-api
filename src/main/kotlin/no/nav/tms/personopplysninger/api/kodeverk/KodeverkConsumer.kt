package no.nav.tms.personopplysninger.api.kodeverk

import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.tms.personopplysninger.api.common.ConsumerException
import no.nav.tms.personopplysninger.api.common.ConsumerMetrics
import no.nav.tms.personopplysninger.api.common.HeaderHelper.addNavHeaders
import no.nav.tms.personopplysninger.api.common.HeaderHelper.authorization
import no.nav.tms.token.support.entraid.token.fetcher.EntraIdTokenFetcher
import java.time.Duration

class KodeverkConsumer(
    private val client: HttpClient,
    private val entraIdTokenFetcher: EntraIdTokenFetcher,
    private val kodeverkUrl: String,
    private val kodevekClientId: String,
    cacheDuration: Duration = Duration.ofMinutes(45)
) {
    private val cache = Caffeine.newBuilder()
        .maximumSize(20)
        .expireAfterWrite(cacheDuration)
        .build<Pair<String, Boolean>, KodeverkBetydningerResponse>()

    private val metrics = ConsumerMetrics.init { }

    fun hentRetningsnumre(): KodeverkBetydningerResponse {
        return getKodeverk("Retningsnumre")
    }

    fun hentKommuner(): KodeverkBetydningerResponse {
        return getKodeverk("Kommuner", ekskluderUgyldige = false)
    }

    fun hentLandKoder(): KodeverkBetydningerResponse {
        return getKodeverk("Landkoder", ekskluderUgyldige = false)
    }

    fun hentPostnummer(): KodeverkBetydningerResponse {
        return getKodeverk("Postnummer")
    }

    fun hentStatsborgerskap(): KodeverkBetydningerResponse {
        return getKodeverk("StatsborgerskapFreg")
    }

    fun hentDekningMedl(): KodeverkBetydningerResponse {
        return getKodeverk("DekningMedl")
    }

    fun hentGrunnlagMedl(): KodeverkBetydningerResponse {
        return getKodeverk("GrunnlagMedl")
    }

    fun hentSpraak(): KodeverkBetydningerResponse {
        return getKodeverk("Språk")
    }

    fun hentTema(): KodeverkBetydningerResponse {
        return getKodeverk("Tema")
    }

    private fun getKodeverk(navn: String, ekskluderUgyldige: Boolean = true): KodeverkBetydningerResponse {
        return cache.get(navn to ekskluderUgyldige) {
            runBlocking(Dispatchers.IO) {
                fetchKodeverk(navn, ekskluderUgyldige)
            }
        }
    }

    private suspend fun fetchKodeverk(navn: String, ekskluderUgyldige: Boolean): KodeverkBetydningerResponse {

        val response = metrics.measureRequest(navn.lowercase()) {
            client.get("$kodeverkUrl/api/v1/kodeverk") {
                url { // Ensures correct encoding of non-ascii characters
                    appendPathSegments(navn, "koder", "betydninger")
                }
                parameter(PARAM_SPRAAK, NORSK_BOKMAAL)
                parameter(PARAM_EKSKLUDER_UGYLDIGE, ekskluderUgyldige)
                addNavHeaders()
                authorization(entraIdTokenFetcher.getAccessToken(kodevekClientId))
            }
        }

        return if (response.status.isSuccess()) {
            response.body<KodeverkBetydningerResponse>()
        } else {
            throw ConsumerException.fromResponse(externalService = "kodeverk-api", response)
        }
    }

    companion object {
        private const val PARAM_SPRAAK = "spraak"
        private const val PARAM_EKSKLUDER_UGYLDIGE = "ekskluderUgyldige"
        private const val NORSK_BOKMAAL = "nb"
    }
}
