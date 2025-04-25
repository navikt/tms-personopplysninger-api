package no.nav.tms.personopplysninger.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.tms.common.util.config.StringEnvVar.getEnvVar

data class Environment(
    val corsAllowedOrigins: String = System.getenv("CORS_ALLOWED_ORIGINS"),
    val corsAllowedSchemes: String = System.getenv("CORS_ALLOWED_SCHEMES"),

    val inst2Url: String = System.getenv("INST2_API_URL"),
    val kodeverkUrl: String = System.getenv("KODEVERK_REST_API_URL"),
    val norg2Url: String = System.getenv("NORG2_API_URL"),
    val digdirKrrProxyUrl: String = System.getenv("DIGDIR_KRR_PROXY_URL"),
    val pdlMottakUrl: String = System.getenv("PDL_MOTTAK_API_URL"),
    val pdlUrl: String = System.getenv("PDL_API_URL"),
    val medlUrl: String = System.getenv("MEDLEMSKAP_MEDL_API_URL"),
    val kontoregisterUrl: String = System.getenv("KONTOREGISTER_URL"),

    val inst2ClientId: String = System.getenv("INST2_TARGET_APP"),
    val digdirKrrProxyClientId: String = System.getenv("DIGDIR_KRR_PROXY_CONSUMER_TARGET_APP"),
    val medlClientId: String = System.getenv("MEDL_TARGET_APP"),
    val pdlClientId: String = System.getenv("PDL_CONSUMER_TARGET_APP"),
    val pdlMottakClientId: String = System.getenv("PDL_MOTTAK_TARGET_APP"),
    val kontoregisterClientId: String = System.getenv("KONTOREGISTER_TARGET_APP"),
    val kodeverkClientId: String = System.getenv("KODEVERK_TARGET_APP"),

    val pdlBehandlingsnummer: String = "B258"
)

object HttpClientBuilder {

    fun build(httpClientEngine: HttpClientEngine = Apache.create()): HttpClient {
        return HttpClient(httpClientEngine) {
            install(ContentNegotiation) {
                jackson { jsonConfig() }
            }
            install(HttpTimeout)
        }
    }
}
