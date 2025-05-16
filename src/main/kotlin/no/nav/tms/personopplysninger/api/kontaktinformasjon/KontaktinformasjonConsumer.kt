package no.nav.tms.personopplysninger.api.kontaktinformasjon

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import no.nav.tms.personopplysninger.api.UserPrincipal
import no.nav.tms.personopplysninger.api.common.ConsumerException
import no.nav.tms.personopplysninger.api.common.ConsumerMetrics
import no.nav.tms.personopplysninger.api.common.HeaderHelper.addNavHeaders
import no.nav.tms.personopplysninger.api.common.HeaderHelper.authorization
import no.nav.tms.personopplysninger.api.common.TokenExchanger

class KontaktinformasjonConsumer(
    private val client: HttpClient,
    private val krrProxyUrl: String,
    private val tokenExchanger: TokenExchanger,
) {

    private val metrics = ConsumerMetrics.init { }

    suspend fun hentKontaktinformasjon(user: UserPrincipal): DigitalKontaktinformasjon {

        val response = metrics.measureRequest("kontaktinformasjon") {
            client.get("$krrProxyUrl/rest/v1/person") {
                authorization(tokenExchanger.krrProxyToken(user.accessToken))
                addNavHeaders(user.ident)
            }
        }

        return if (response.status.isSuccess()) {
            response.body()
        } else {
            throw ConsumerException.fromResponse(externalService = "krr-proxy", response)
        }
    }
}
