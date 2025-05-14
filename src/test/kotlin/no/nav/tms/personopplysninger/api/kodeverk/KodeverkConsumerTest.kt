package no.nav.tms.personopplysninger.api.kodeverk

import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.tms.personopplysninger.api.jsonConfig
import no.nav.tms.token.support.azure.exchange.AzureService
import org.junit.jupiter.api.Test
import java.time.Duration

class KodeverkConsumerTest {

    private val kodeverkUrl = "http://kodeverk"
    private val kodeverkClientId = "clientId"

    private val azureService = mockk<AzureService>().also {
        coEvery { it.getAccessToken(kodeverkClientId) } returns "token"
    }

    @Test
    fun `bruker cachede verdier ved repeterte forespørsel etter samme data`() = runBlocking<Unit> {
        var counter = 0

        val httpClient = setupClient {
            counter++

            respond(
                content = """{"betydninger": {}}""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }


        val consumer = KodeverkConsumer(httpClient, azureService, kodeverkUrl, kodeverkClientId)


        consumer.hentPostnummer()

        counter shouldBe 1

        consumer.hentPostnummer()

        counter shouldBe 1

        consumer.hentSpraak()

        counter shouldBe 2
    }

    @Test
    fun `refresher cache dersom data er markert stale`() = runBlocking<Unit> {
        var counter = 0

        val httpClient = setupClient {
            counter++

            respond(
                content = """{"betydninger": {}}""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }


        val consumer = KodeverkConsumer(
            httpClient, azureService, kodeverkUrl, kodeverkClientId,
            cacheDuration = Duration.ofMillis(200)
        )


        consumer.hentPostnummer()
        consumer.hentPostnummer()

        counter shouldBe 1

        delay(250)

        consumer.hentPostnummer()

        counter shouldBe 2
    }

    private fun setupClient(handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        HttpClient(MockEngine) {
            engine {
                addHandler(handler)
            }
            install(ContentNegotiation) {
                jackson { jsonConfig() }
            }
        }

}
