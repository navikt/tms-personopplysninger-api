package no.nav.tms.personopplysninger.api.common;

import no.nav.tms.token.support.tokendings.exchange.TokendingsService

class TokenExchanger(
    private val tokendingsService: TokendingsService,
    private val kontoregisterClientId: String,
    private val pdlClientId: String,
    private val medlClientId: String,
    private val inst2ClientId: String,
) {
    suspend fun pdlToken(accessToken: String): String {
        return tokendingsService.exchangeToken(accessToken, pdlClientId)
    }

    suspend fun kontoregisterToken(accessToken: String): String {
        return tokendingsService.exchangeToken(accessToken, kontoregisterClientId)
    }

    suspend fun medlToken(accessToken: String): String {
        return tokendingsService.exchangeToken(accessToken, medlClientId)
    }

    suspend fun inst2Token(accessToken: String): String {
        return tokendingsService.exchangeToken(accessToken, inst2ClientId)
    }
}
