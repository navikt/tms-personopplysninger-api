package no.nav.tms.personopplysninger.api

import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.*
import no.nav.tms.personopplysninger.api.common.TokenExchanger
import no.nav.tms.personopplysninger.api.institusjon.InstitusjonConsumer
import no.nav.tms.personopplysninger.api.institusjon.institusjonRoutes
import no.nav.tms.personopplysninger.api.kodeverk.KodeverkConsumer
import no.nav.tms.personopplysninger.api.kodeverk.kodeverkRoutes
import no.nav.tms.personopplysninger.api.kontaktinformasjon.KontaktinformasjonConsumer
import no.nav.tms.personopplysninger.api.kontaktinformasjon.KontaktinformasjonService
import no.nav.tms.personopplysninger.api.kontaktinformasjon.kontaktinformasjonRoutes
import no.nav.tms.personopplysninger.api.medl.MedlConsumer
import no.nav.tms.personopplysninger.api.medl.MedlService
import no.nav.tms.personopplysninger.api.medl.medlRoutes
import no.nav.tms.personopplysninger.api.kontoregister.KontoregisterConsumer
import no.nav.tms.personopplysninger.api.kontoregister.kontoregisterRoutes
import no.nav.tms.personopplysninger.api.personalia.pdl.PdlApiConsumer
import no.nav.tms.personopplysninger.api.personalia.HentPersonaliaService
import no.nav.tms.personopplysninger.api.personalia.OppdaterPersonaliaService
import no.nav.tms.personopplysninger.api.personalia.norg2.Norg2Consumer
import no.nav.tms.personopplysninger.api.personalia.pdl.PdlMottakConsumer
import no.nav.tms.personopplysninger.api.personalia.personalia
import no.nav.tms.personopplysninger.api.sporingslogg.EregServicesConsumer
import no.nav.tms.personopplysninger.api.sporingslogg.SporingsloggConsumer
import no.nav.tms.personopplysninger.api.sporingslogg.SporingsloggService
import no.nav.tms.personopplysninger.api.sporingslogg.sporingsloggRoutes
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
        krrProxyClientId = environment.digdirKrrProxyClientId,
        sporingsloggClientId = environment.sporingsloggClientId
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
        kontaktinfoConsumer = KontaktinformasjonConsumer(httpClient, environment.digdirKrrProxyUrl, tokenExchanger),
        kodeverkConsumer = kodeverkConsumer
    )

    val sporingsloggService = SporingsloggService(
        sporingsloggConsumer = SporingsloggConsumer(httpClient, environment.sporingsloggUrl, tokenExchanger),
        eregServicesConsumer = EregServicesConsumer(httpClient, environment.eregServicesUrl),
        kodeverkConsumer = kodeverkConsumer
    )

    val userRoutes: Route.() -> Unit = {
        personalia(hentPersonaliaService, oppdaterPersonaliaService)
        medlRoutes(medlService)
        institusjonRoutes(institusjonConsumer)
        kontaktinformasjonRoutes(kontaktinformasjonService)
        kontoregisterRoutes(kontoregisterConsumer)
        kodeverkRoutes(kodeverkConsumer)
        sporingsloggRoutes(sporingsloggService)
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
            mainModule(
                userRoutes = userRoutes,
                httpClient = httpClient,
                corsAllowedOrigins = environment.corsAllowedOrigins,
                corsAllowedSchemes = environment.corsAllowedSchemes
            )
        }
    ).start(wait = true)
}
