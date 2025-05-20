package no.nav.tms.personopplysninger.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.*

data class Environment(
    val corsAllowedOrigins: String = "*.nav.no",
    val corsAllowedSchemes: String = "https",

    val inst2Url: String = System.getenv("INST2_API_URL"),
    val kodeverkUrl: String = System.getenv("KODEVERK_REST_API_URL"),
    val norg2Url: String = System.getenv("NORG2_API_URL"),
    val digdirKrrProxyUrl: String = System.getenv("DIGDIR_KRR_PROXY_URL"),
    val pdlMottakUrl: String = System.getenv("PDL_MOTTAK_API_URL"),
    val pdlUrl: String = System.getenv("PDL_API_URL"),
    val medlUrl: String = System.getenv("MEDLEMSKAP_MEDL_API_URL"),
    val kontoregisterUrl: String = System.getenv("KONTOREGISTER_URL"),

    val inst2ClientId: String = System.getenv("INST2_CLIENT_ID"),
    val digdirKrrProxyClientId: String = System.getenv("DIGDIR_KRR_PROXY_CONSUMER_CLIENT_ID"),
    val medlClientId: String = System.getenv("MEDL_CLIENT_ID"),
    val pdlApiClientId: String = System.getenv("PDL_CONSUMER_CLIENT_ID"),
    val pdlMottakClientId: String = System.getenv("PDL_MOTTAK_CLIENT_ID"),
    val kontoregisterClientId: String = System.getenv("KONTOREGISTER_CLIENT_ID"),
    val kodeverkClientId: String = System.getenv("KODEVERK_CLIENT_ID"),

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
