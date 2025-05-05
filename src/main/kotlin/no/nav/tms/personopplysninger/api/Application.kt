package no.nav.tms.personopplysninger.api

import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.*
import no.nav.tms.personopplysninger.api.common.TokenExchanger
import no.nav.tms.personopplysninger.api.institusjon.InstitusjonConsumer
import no.nav.tms.personopplysninger.api.institusjon.institusjon
import no.nav.tms.personopplysninger.api.kodeverk.KodeverkConsumer
import no.nav.tms.personopplysninger.api.kontaktinformasjon.KontaktinfoConsumer
import no.nav.tms.personopplysninger.api.kontaktinformasjon.KontaktinformasjonService
import no.nav.tms.personopplysninger.api.kontaktinformasjon.kontaktinformasjon
import no.nav.tms.personopplysninger.api.medl.MedlConsumer
import no.nav.tms.personopplysninger.api.medl.MedlService
import no.nav.tms.personopplysninger.api.medl.medl
import no.nav.tms.personopplysninger.api.personalia.KontoregisterConsumer
import no.nav.tms.personopplysninger.api.personalia.PdlConsumer
import no.nav.tms.personopplysninger.api.personalia.PersonaliaService
import no.nav.tms.personopplysninger.api.personalia.norg2.Norg2Consumer
import no.nav.tms.personopplysninger.api.personalia.personalia
import no.nav.tms.token.support.azure.exchange.AzureServiceBuilder
import no.nav.tms.token.support.tokendings.exchange.TokendingsServiceBuilder

fun main() {
    val environment = Environment()

    val httpClient = HttpClientBuilder.build()

    val azureService = AzureServiceBuilder.buildAzureService()
    val tokenExchanger = TokenExchanger(
        tokendingsService = TokendingsServiceBuilder.buildTokendingsService(),
        kontoregisterClientId = environment.kontoregisterClientId,
        pdlClientId = environment.pdlClientId,
        medlClientId = environment.medlClientId,
        inst2ClientId = environment.inst2ClientId,
        krrProxyClientId = environment.digdirKrrProxyClientId
    )

    val kodeverkConsumer = KodeverkConsumer(httpClient, azureService, environment.kodeverkUrl, environment.kodeverkClientId)

    val personaliaService = PersonaliaService(
        kodeverkConsumer = kodeverkConsumer,
        norg2Consumer = Norg2Consumer(httpClient, environment.norg2Url),
        kontoregisterConsumer = KontoregisterConsumer(httpClient, environment.kontoregisterUrl, tokenExchanger),
        pdlConsumer = PdlConsumer(httpClient, environment.pdlUrl, environment.pdlBehandlingsnummer, tokenExchanger),
    )

    val medlService = MedlService(
        medlConsumer = MedlConsumer(httpClient, environment.medlUrl, tokenExchanger),
        kodeverkConsumer = kodeverkConsumer
    )

    val institusjonConsumer = InstitusjonConsumer(
        client = httpClient,
        inst2Url = environment.inst2Url,
        tokenExchanger = tokenExchanger,
    )

    val kontaktinformasjonService = KontaktinformasjonService(
        kontaktinfoConsumer = KontaktinfoConsumer(httpClient, environment.digdirKrrProxyUrl, tokenExchanger),
        kodeverkConsumer = kodeverkConsumer
    )

    val userRoutes: Route.() -> Unit = {
        personalia(personaliaService)
        medl(medlService)
        institusjon(institusjonConsumer)
        kontaktinformasjon(kontaktinformasjonService)
    }

    embeddedServer(
        factory = Netty,
        configure = {
            connector {
                port = 8080
            }
        },
        module = {
            rootPath = "tms-personopplysninger-api"
            mainModule(userRoutes, httpClient)
        }
    ).start(wait = true)
}
