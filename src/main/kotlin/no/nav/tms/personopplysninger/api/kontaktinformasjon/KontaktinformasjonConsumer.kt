package no.nav.tms.personopplysninger.api.kontaktinformasjon

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
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
            client.post("$krrProxyUrl/rest/v1/personer") {
                authorization(tokenExchanger.krrProxyToken(user.accessToken))
                addNavHeaders()
                contentType(ContentType.Application.Json)
                setBody(PersonerRequest(listOf(user.ident)))
            }
        }

        return if (response.status.isSuccess()) {
            response.body<PersonerResponse>()
                .personer[user.ident] ?: throw IllegalStateException("Fant ikke bruker i svar fra krr")
        } else {
            throw ConsumerException.fromResponse(externalService = "krr-proxy", response)
        }
    }

    private data class PersonerRequest(
        val personidenter: List<String>
    )

    private data class PersonerResponse(
        val personer: Map<String, DigitalKontaktinformasjon>
    )
}
