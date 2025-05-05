package no.nav.tms.personopplysninger.api.kontaktinformasjon

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.*
import io.ktor.http.isSuccess
import no.nav.tms.personopplysninger.api.UserPrincipal
import no.nav.tms.personopplysninger.api.common.HeaderHelper.addNavHeaders
import no.nav.tms.personopplysninger.api.common.HeaderHelper.authorization
import no.nav.tms.personopplysninger.api.common.TokenExchanger

class KontaktinfoConsumer(
    private val client: HttpClient,
    private val krrProxyUrl: String,
    private val tokenExchanger: TokenExchanger,
) {
    suspend fun hentKontaktinformasjon(user: UserPrincipal): DigitalKontaktinformasjon {
        val response: HttpResponse =
            client.get("$krrProxyUrl/rest/v1/person") {
                authorization(tokenExchanger.krrProxyToken(user.accessToken))
                addNavHeaders()
                header("Nav-Personident", user.ident)
            }

        return if (response.status.isSuccess()) {
            response.body()
        } else {
            throw KontaktinfoConsumerException(response.request.url.toString(), response.status.value, response.bodyAsText())
        }
    }
}

class KontaktinfoConsumerException(val endpoint: String, val status: Int, message: String): RuntimeException()
