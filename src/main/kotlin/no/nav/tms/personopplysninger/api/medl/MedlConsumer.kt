package no.nav.tms.personopplysninger.api.medl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.isSuccess
import no.nav.tms.personopplysninger.api.UserPrincipal
import no.nav.tms.personopplysninger.api.common.HeaderHelper.addNavHeaders
import no.nav.tms.personopplysninger.api.common.HeaderHelper.authorization
import no.nav.tms.personopplysninger.api.common.TokenExchanger

class MedlConsumer(
    private val client: HttpClient,
    private val medlUrl: String,
    private val tokenExchanger: TokenExchanger,
) {
    suspend fun hentMedlemskap(user: UserPrincipal): Medlemskapsunntak {
        val response =
            client.post {
                url("$medlUrl/rest/v1/innsyn")
                setBody(MedlRequest(personident = user.ident))
                authorization(tokenExchanger.medlToken(user.accessToken))
                addNavHeaders()
            }
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            throw MedlConsumerException(response.request.url.toString(), response.status.value, response.bodyAsText())
        }
    }
}

class MedlConsumerException(val endpoint: String, val status: Int, message: String): RuntimeException()

private data class MedlRequest(val personident: String)
