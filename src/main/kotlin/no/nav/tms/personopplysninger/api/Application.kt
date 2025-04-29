package no.nav.tms.personopplysninger.api

import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.tms.personopplysninger.api.common.TokenExchanger
import no.nav.tms.personopplysninger.api.kodeverk.KodeverkConsumer
import no.nav.tms.personopplysninger.api.personalia.KontoregisterConsumer
import no.nav.tms.personopplysninger.api.personalia.PdlConsumer
import no.nav.tms.personopplysninger.api.personalia.PersonaliaService
import no.nav.tms.personopplysninger.api.personalia.norg2.Norg2Consumer
import no.nav.tms.token.support.azure.exchange.AzureService
import no.nav.tms.token.support.azure.exchange.AzureServiceBuilder
import no.nav.tms.token.support.tokendings.exchange.TokendingsServiceBuilder
import kotlin.math.hypot

fun main() {
    val environment = Environment()

    val httpClient = HttpClientBuilder.build()

    val azureService = AzureServiceBuilder.buildAzureService()
    val tokenExchanger = TokenExchanger(
        tokendingsService = TokendingsServiceBuilder.buildTokendingsService(),
        kontoregisterClientId = environment.kontoregisterClientId,
        pdlClientId = environment.pdlClientId
    )

    val personaliaService = PersonaliaService(
        kodeverkConsumer = KodeverkConsumer(httpClient, azureService, environment.kodeverkUrl, environment.kodeverkClientId),
        norg2Consumer = Norg2Consumer(httpClient, environment.norg2Url),
        kontoregisterConsumer = KontoregisterConsumer(httpClient, environment.kontoregisterUrl, tokenExchanger),
        pdlConsumer = PdlConsumer(httpClient, environment.pdlUrl, environment.pdlBehandlingsnummer, tokenExchanger),
    )

    embeddedServer(
        factory = Netty,
        configure = {
            connector {
                port = 8080
            }
        },
        module = {
            rootPath = "tms-personopplysninger-api"
            mainModule(personaliaService, httpClient)
        }
    ).start(wait = true)
}
