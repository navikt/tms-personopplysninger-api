package no.nav.tms.personopplysninger.api.sporingslogg

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import no.nav.tms.personopplysninger.api.UserPrincipal
import no.nav.tms.personopplysninger.api.common.ConsumerException
import no.nav.tms.personopplysninger.api.common.ConsumerMetrics
import no.nav.tms.personopplysninger.api.common.HeaderHelper.authorization
import no.nav.tms.personopplysninger.api.common.TokenExchanger
import java.time.LocalDateTime

class SporingsloggConsumer(
    private val client: HttpClient,
    private val sporingsloggUrl: String,
    private val tokenExchanger: TokenExchanger
) {

    private val metrics = ConsumerMetrics.init { }

    suspend fun getSporingslogg(user: UserPrincipal): List<Sporingslogg> {

        val sporingsloggResponse = metrics.measureRequest("logg") {
            client.get("$sporingsloggUrl/api/les") {
                authorization(tokenExchanger.sporingsloggToken(user.accessToken))
            }
        }

        if (sporingsloggResponse.status.isSuccess()) {
            return sporingsloggResponse.body()
        } else {
            throw ConsumerException.fromResponse(externalService = "sporingslogg", sporingsloggResponse)
        }
    }

    data class Sporingslogg(
        val tema: String,
        val uthentingsTidspunkt: LocalDateTime? = null,
        val mottaker: String,
        val leverteData: String? = null,
        val samtykkeToken: String? = null
    )
}
