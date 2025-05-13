package no.nav.tms.personopplysninger.api

import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.*
import no.nav.tms.personopplysninger.api.common.TokenExchanger
import no.nav.tms.personopplysninger.api.institusjon.InstitusjonConsumer
import no.nav.tms.personopplysninger.api.institusjon.institusjonRoute
import no.nav.tms.personopplysninger.api.kodeverk.KodeverkConsumer
import no.nav.tms.personopplysninger.api.kontaktinformasjon.KontaktinfoConsumer
import no.nav.tms.personopplysninger.api.kontaktinformasjon.KontaktinformasjonService
import no.nav.tms.personopplysninger.api.kontaktinformasjon.kontaktinformasjon
import no.nav.tms.personopplysninger.api.medl.MedlConsumer
import no.nav.tms.personopplysninger.api.medl.MedlService
import no.nav.tms.personopplysninger.api.medl.medl
import no.nav.tms.personopplysninger.api.kontoregister.KontoregisterConsumer
import no.nav.tms.personopplysninger.api.kontoregister.kontoregisterRoute
import no.nav.tms.personopplysninger.api.personalia.pdl.PdlApiConsumer
import no.nav.tms.personopplysninger.api.personalia.HentPersonaliaService
import no.nav.tms.personopplysninger.api.personalia.OppdaterPersonaliaService
import no.nav.tms.personopplysninger.api.personalia.norg2.Norg2Consumer
import no.nav.tms.personopplysninger.api.personalia.pdl.PdlMottakConsumer
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
        pdlApiClientId = environment.pdlApiClientId,
        pdlMottakClientId = environment.pdlMottakClientId,
        medlClientId = environment.medlClientId,
        inst2ClientId = environment.inst2ClientId,
        krrProxyClientId = environment.digdirKrrProxyClientId
    )

    val kodeverkConsumer = KodeverkConsumer(httpClient, azureService, environment.kodeverkUrl, environment.kodeverkClientId)

    val pdlApiConsumer = PdlApiConsumer(httpClient, environment.pdlUrl, environment.pdlBehandlingsnummer, tokenExchanger)
    val pdlMottakConsumer = PdlMottakConsumer(httpClient, environment.pdlMottakUrl, tokenExchanger)

    val kontoregisterConsumer = KontoregisterConsumer(httpClient, environment.kontoregisterUrl, tokenExchanger)

    val hentPersonaliaService = HentPersonaliaService(
        kodeverkConsumer = kodeverkConsumer,
        norg2Consumer = Norg2Consumer(httpClient, environment.norg2Url),
        kontoregisterConsumer = kontoregisterConsumer,
        pdlApiConsumer = pdlApiConsumer,
    )

    val oppdaterPersonaliaService = OppdaterPersonaliaService(
        pdlApiConsumer = pdlApiConsumer,
        pdlMottakConsumer = pdlMottakConsumer
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
        personalia(hentPersonaliaService, oppdaterPersonaliaService)
        medl(medlService)
        institusjonRoute(institusjonConsumer)
        kontaktinformasjon(kontaktinformasjonService)
        kontoregisterRoute(kontoregisterConsumer)
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
