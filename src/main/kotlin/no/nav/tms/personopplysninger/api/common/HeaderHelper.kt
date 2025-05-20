package no.nav.tms.personopplysninger.api.common

import io.ktor.client.request.*
import io.ktor.http.*
import java.util.UUID

object HeaderHelper {
    const val CALL_ID_HEADER = "Nav-Call-Id"
    const val NAV_CONSUMER_ID_HEADER = "Nav-Consumer-Id"
    const val NAV_CONSUMER_ID = "tms-personopplysninger-api"
    const val NAV_PERSONIDENT_HEADER = "Nav-Personident"

    fun HttpRequestBuilder.addNavHeaders(ident: String? = null) {
        header(CALL_ID_HEADER, UUID.randomUUID().toString())
        header(NAV_CONSUMER_ID_HEADER, NAV_CONSUMER_ID)

        if (ident != null) {
            header(NAV_PERSONIDENT_HEADER, ident)
        }
    }

    fun HttpRequestBuilder.authorization(token: String) {
        header(HttpHeaders.Authorization, "Bearer $token")
    }
}
