package no.nav.tms.personopplysninger.api.kontoregister

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
import no.nav.tms.personopplysninger.api.common.TokenExchanger
import no.nav.tms.personopplysninger.api.jsonConfig
import no.nav.tms.token.support.azure.exchange.AzureService
import org.junit.jupiter.api.Test
import java.time.Duration

class KontoregisterConsumerTest {

    private val kontoregisterUrl = "http://kontoregister"

    private val tokenExchanger = mockk<TokenExchanger>().also {
        coEvery { it.kontoregisterToken(any()) } returns "token"
    }

    @Test
    fun `bruker cachede verdier ved repeterte forespørsel etter landkoder`() = runBlocking<Unit> {
        var counter = 0

        val httpClient = setupClient {
            counter++

            respond(
                content = dummyLandkoder,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val consumer = KontoregisterConsumer(httpClient, kontoregisterUrl, tokenExchanger)

        consumer.hentLandkoder()

        counter shouldBe 1

        consumer.hentLandkoder()
        consumer.hentLandkoder()

        counter shouldBe 1
    }

    @Test
    fun `bruker cachede verdier ved repeterte forespørsel etter valutakoder`() = runBlocking<Unit> {
        var counter = 0

        val httpClient = setupClient {
            counter++

            respond(
                content = dummyValutakoder,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val consumer = KontoregisterConsumer(httpClient, kontoregisterUrl, tokenExchanger)

        consumer.hentValutakoder()

        counter shouldBe 1

        consumer.hentValutakoder()
        consumer.hentValutakoder()

        counter shouldBe 1
    }

    @Test
    fun `refresher cache dersom data er markert stale`() = runBlocking<Unit> {
        var counter = 0

        val httpClient = setupClient {
            counter++

            respond(
                content = "[]",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }


        val consumer = KontoregisterConsumer(
            httpClient, kontoregisterUrl, tokenExchanger,
            cacheDuration = Duration.ofMillis(200)
        )

        consumer.hentLandkoder()

        counter shouldBe 1

        consumer.hentValutakoder()

        counter shouldBe 2

        consumer.hentLandkoder()
        consumer.hentValutakoder()

        counter shouldBe 2

        delay(250)

        consumer.hentLandkoder()
        consumer.hentValutakoder()

        counter shouldBe 4
    }

    private val dummyLandkoder = """
[
    {
        "landkode": "DUM",
        "land": "Dummy",
        "kreverIban": false,
        "kreverBankkode": false
    }
]
    """.trimIndent()

    private val dummyValutakoder = """
[
    {
        "valutakode" : "DUM",
        "valuta" : "Dummy"
    }
]
    """.trimIndent()

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
