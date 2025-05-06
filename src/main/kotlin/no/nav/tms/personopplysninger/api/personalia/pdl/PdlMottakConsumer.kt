package no.nav.tms.personopplysninger.api.personalia.pdl

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import no.nav.tms.personopplysninger.api.UserPrincipal
import no.nav.tms.personopplysninger.api.common.ConsumerException
import no.nav.tms.personopplysninger.api.common.HeaderHelper.addNavHeaders
import no.nav.tms.personopplysninger.api.common.HeaderHelper.authorization
import no.nav.tms.personopplysninger.api.common.TokenExchanger
import no.nav.tms.personopplysninger.api.personalia.Endring
import no.nav.tms.personopplysninger.api.personalia.TelefonnummerEndring
import no.nav.tms.personopplysninger.api.personalia.pdl.OppdaterTelefonnummer.Companion.endreTelefonnummerPayload
import no.nav.tms.personopplysninger.api.personalia.pdl.OppdaterTelefonnummer.Companion.slettTelefonnummerPayload

class PdlMottakConsumer(
    private val client: HttpClient,
    private val pdlMottakUrl: String,
    private val tokenExchanger: TokenExchanger,
) {
    private val log = KotlinLogging.logger {}

    suspend fun endreTelefonnummer(user: UserPrincipal, telefonnummer: TelefonnummerEndring): Endring {
        val payload = endreTelefonnummerPayload(user.ident, telefonnummer)
        return sendPdlEndring(user, payload)
    }

    suspend fun slettTelefonnummer(user: UserPrincipal, opplysningsId: String): Endring {
        val payload = slettTelefonnummerPayload(user.ident, opplysningsId)
        return sendPdlEndring(user, payload)
    }

    private suspend fun sendPdlEndring(user: UserPrincipal, payload: OppdaterTelefonnummer): Endring {
        val exchangedToken = tokenExchanger.pdlMottakToken(user.accessToken)

        val response =
            client.post {
                url("$pdlMottakUrl/api/v1/endringer")
                authorization(exchangedToken)
                addNavHeaders()
                contentType(ContentType.Application.Json)
                setBody(PersonEndring(payload))
            }

        return try {
            readResponseAndPollStatus(exchangedToken, response)
        } catch (e: Exception) {
            throw ConsumerException.fromResponse("pdl-mottak", response)
        }
    }

    private suspend fun readResponseAndPollStatus(accessToken: String, response: HttpResponse): Endring {
        return when {
            response.status == HttpStatusCode.Locked -> {
                log.info {"Oppdatering avvist pga status pending." }
                Endring(statusType = "REJECTED", error = response.body())
            }

            response.status == HttpStatusCode.UnprocessableEntity -> {
                val responseBody = response.bodyAsText()
                log.error {"Fikk valideringsfeil: $responseBody" }
                Endring(statusType = "ERROR", error = response.body())
            }

            !response.status.isSuccess() -> {
                throw ConsumerException.fromResponse("pdl-mottak", response)
            }

            else -> {
                val location = response.headers[HttpHeaders.Location]
                    ?: throw RuntimeException("Fant ikke Location-header i respons fra Pdl-mottak")
                val pollEndringUrl = "$pdlMottakUrl$location"
                pollEndring(accessToken, pollEndringUrl)
            }
        }
    }

    private suspend fun pollEndring(accessToken: String, url: String): Endring {
        var endring: Endring
        var i = 0
        do {
            try {
                delay(1000)
            } catch (ie: InterruptedException) {
                throw RuntimeException("Fikk feil under polling på status", ie)
            }

            val response: HttpResponse =
                client.get(url) {
                    authorization(accessToken)

                }
            endring = response.body<List<Endring>>().first()
        } while (++i < 5 && endring.isPending())
        log.info { "Antall polls for status: $i" }

        if (!endring.confirmedOk()) {
            if (endring.hasTpsError()) {
                endring.addValidationError()
            } else {
                log.warn { "Polling timet ut før endring ble bekreftet OK av pdl-mottak" }
            }
        }
        return endring
    }
}

private data class PersonEndring(val personopplysninger: List<OppdaterTelefonnummer>) {
    constructor(personopplysning: OppdaterTelefonnummer) : this(listOf(personopplysning))
}
