package no.nav.tms.personopplysninger.api.kodeverk

import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.tms.personopplysninger.api.common.ConsumerException
import no.nav.tms.personopplysninger.api.common.HeaderHelper.addNavHeaders
import no.nav.tms.personopplysninger.api.common.HeaderHelper.authorization
import no.nav.tms.token.support.azure.exchange.AzureService
import java.time.Duration

class KodeverkConsumer(
    private val client: HttpClient,
    private val azureService: AzureService,
    private val kodeverkUrl: String,
    private val kodevekClientId: String,
) {
    private val cache = Caffeine.newBuilder()
        .maximumSize(20)
        .expireAfterWrite(Duration.ofMinutes(45))
        .build<Pair<String, Boolean>, KodeverkBetydningerResponse>()

    suspend fun hentRetningsnumre(): KodeverkBetydningerResponse {
        return getKodeverk("Retningsnumre")
    }

    suspend fun hentKommuner(): KodeverkBetydningerResponse {
        return getKodeverk("Kommuner", ekskluderUgyldige = false)
    }

    suspend fun hentLandKoder(): KodeverkBetydningerResponse {
        return getKodeverk("Landkoder", ekskluderUgyldige = false)
    }

    suspend fun hentPostnummer(): KodeverkBetydningerResponse {
        return getKodeverk("Postnummer")
    }

    suspend fun hentStatsborgerskap(): KodeverkBetydningerResponse {
        return getKodeverk("StatsborgerskapFreg")
    }

    suspend fun hentDekningMedl(): KodeverkBetydningerResponse {
        return getKodeverk("DekningMedl")
    }

    suspend fun hentGrunnlagMedl(): KodeverkBetydningerResponse {
        return getKodeverk("GrunnlagMedl")
    }

    suspend fun hentSpraak(): KodeverkBetydningerResponse {
        return getKodeverk("Språk")
    }

    private suspend fun getKodeverk(navn: String, ekskluderUgyldige: Boolean = true): KodeverkBetydningerResponse {
        return cache.get(navn to ekskluderUgyldige) {
            runBlocking(Dispatchers.IO) {
                fetchKodeverk(navn, ekskluderUgyldige)
            }
        }
    }

    private suspend fun fetchKodeverk(navn: String, ekskluderUgyldige: Boolean): KodeverkBetydningerResponse {
        client.get {
            url("$kodeverkUrl/api/v1/kodeverk/$navn/koder/betydninger")
            parameter(PARAM_SPRAAK, NORSK_BOKMAAL)
            parameter(PARAM_EKSKLUDER_UGYLDIGE, ekskluderUgyldige)
            addNavHeaders()
            authorization(azureService.getAccessToken(kodevekClientId))
        }.let { response ->
            return if (response.status.isSuccess()) {
                response.body<KodeverkBetydningerResponse>()
            } else {
                throw ConsumerException.fromResponse(externalService = "kodeverk-api", response)
            }
        }
    }

    companion object {
        private const val PARAM_SPRAAK = "spraak"
        private const val PARAM_EKSKLUDER_UGYLDIGE = "ekskluderUgyldige"
        private const val NORSK_BOKMAAL = "nb"
    }
}
