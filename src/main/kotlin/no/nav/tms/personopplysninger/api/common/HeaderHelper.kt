package no.nav.tms.personopplysninger.api.common

import io.ktor.client.request.*
import io.ktor.http.*
import java.util.UUID

object HeaderHelper {
    const val CALL_ID_HEADER = "Nav-Call-Id"
    const val NAV_CONSUMER_ID_HEADER = "Nav-Consumer-Id"
    const val NAV_CONSUMER_ID = "tms-personopplysninger-api"

    fun HttpRequestBuilder.addNavHeaders() {
        header(CALL_ID_HEADER, UUID.randomUUID().toString())
        header(NAV_CONSUMER_ID_HEADER, NAV_CONSUMER_ID)
    }

    fun HttpRequestBuilder.authorization(token: String) {
        header(HttpHeaders.Authorization, "Bearer $token")
    }
}
