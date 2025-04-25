package no.nav.tms.personopplysninger.api.personalia

import com.expediagroup.graphql.client.types.GraphQLClientRequest
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.pdl.generated.dto.HentPersonQuery
import no.nav.tms.personopplysninger.api.UserPrincipal
import no.nav.tms.personopplysninger.api.common.HeaderHelper.addNavHeaders
import no.nav.tms.personopplysninger.api.common.HeaderHelper.authorization
import no.nav.tms.personopplysninger.api.common.TokenExchanger
import no.nav.tms.token.support.tokenx.validation.user.TokenXUser
import java.net.URL
import java.util.*

class PdlConsumer(
    private val httpClient: HttpClient,
    private val pdlUrl: String,
    private val behandlingsnummer: String,
    private val tokenExchanger: TokenExchanger
) {

    private val log = KotlinLogging.logger {}
    private val secureLog = KotlinLogging.logger("secureLog")

    suspend fun hentPerson(user: UserPrincipal): HentPersonQuery.Result {
        return HentPersonQuery.Variables(ident = user.ident)
            .let { HentPersonQuery(it) }
            .let { executeQuery(it, user.accessToken) }
    }

    private suspend inline fun <reified T : Any> executeQuery(request: GraphQLClientRequest<T>, token: String): T {
        val pdlToken = tokenExchanger.pdlToken(token)

        val rawResponse = sendQuery(request, pdlToken)

        if (!rawResponse.status.isSuccess()) {
            throw RuntimeException("Fikk http-status [${rawResponse.status}] fra PDL.")
        }
        val pdlResponse = parseBody<T>(rawResponse)

        return pdlResponse.data
            ?: throw RuntimeException("Ingen data i resultatet fra SAF.")
    }

    private suspend fun <T : Any> sendQuery(request: GraphQLClientRequest<T>, accessToken: String): HttpResponse =
        withContext(Dispatchers.IO) {

            val callId = UUID.randomUUID()
            log.info { "Sender graphql-spørring med callId=$callId" }

            httpClient.post {
                url("$pdlUrl/graphql")
                method = HttpMethod.Post

                addNavHeaders()
                authorization(accessToken)

                header("behandlingsnummer", behandlingsnummer)

                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(request)
                timeout {
                    socketTimeoutMillis = 25000
                    connectTimeoutMillis = 10000
                    requestTimeoutMillis = 35000
                }
            }
        }

    private suspend inline fun <reified T: Any> parseBody(response: HttpResponse): GraphQLClientResponse<T> = try {
        response.body<GraphQLClientResponse<T>>()
            .also {
                if (it.containsData() && it.containsErrors()) {
                    val baseMsg = "Resultatet inneholdt data og feil, dataene returneres til bruker."
                    log.warn { baseMsg }
                    secureLog.warn {
                        "$baseMsg Feilene var errors: ${it.errors}, extensions: ${it.extensions}"
                    }
                }
            }
    } catch (e: Exception) {
        throw RuntimeException("Klarte ikke tolke respons fra SAF", e)
    }


    private fun GraphQLClientResponse<*>.containsData() = data != null
    private fun GraphQLClientResponse<*>.containsErrors() = errors?.isNotEmpty() == true
}
