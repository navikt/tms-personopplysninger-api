package no.nav.tms.personopplysninger.api.institusjon

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.tms.personopplysninger.api.UserPrincipal
import no.nav.tms.personopplysninger.api.common.ConsumerException
import no.nav.tms.personopplysninger.api.common.HeaderHelper.authorization
import no.nav.tms.personopplysninger.api.common.TokenExchanger
import no.nav.tms.personopplysninger.api.common.HeaderHelper.addNavHeaders

class InstitusjonConsumer(
    private val client: HttpClient,
    private val inst2Url: String,
    private val tokenExchanger: TokenExchanger
) {

    suspend fun hentInstitusjonsopphold(user: UserPrincipal): List<InnsynInstitusjonsopphold> {
        val response: HttpResponse =
            client.post {
                url("$inst2Url/rest/v1/innsyn")
                authorization(tokenExchanger.inst2Token(user.accessToken))
                addNavHeaders()
                contentType(ContentType.Application.Json)
                setBody(InstitusjonRequest(user.ident))
            }
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            throw ConsumerException.fromResponse(externalService = "inst-2", response)
        }
    }
}

private class InstitusjonRequest(val personident: String)

class InstitusjonConsumerException(val endpoint: String, val status: Int, message: String): RuntimeException()
