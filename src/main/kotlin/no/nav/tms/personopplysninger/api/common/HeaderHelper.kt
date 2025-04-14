package no.nav.tms.personopplysninger.api.common

import io.ktor.client.request.*
import io.ktor.http.*
import java.util.UUID

object HeaderHelper {
    private const val CALL_ID_HEADER_NAME = "Nav-Callid"
    private const val NAV_CONSUMER_ID_HEADER_NAME = "Nav-Consumer-Id"
    private const val NAV_CONSUMER_ID = "tms-personopplysninger-api"

    fun HttpRequestBuilder.addNavHeaders() {
        header(CALL_ID_HEADER_NAME, UUID.randomUUID().toString())
        header(NAV_CONSUMER_ID_HEADER_NAME, NAV_CONSUMER_ID)
    }

    fun HttpRequestBuilder.authorization(token: String) {
        header(HttpHeaders.Authorization, "Bearer $token")
    }
}
