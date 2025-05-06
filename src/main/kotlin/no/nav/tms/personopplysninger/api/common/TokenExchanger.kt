package no.nav.tms.personopplysninger.api.common;

import no.nav.tms.token.support.tokendings.exchange.TokendingsService

class TokenExchanger(
    private val tokendingsService: TokendingsService,
    private val kontoregisterClientId: String,
    private val pdlApiClientId: String,
    private val pdlMottakClientId: String,
    private val medlClientId: String,
    private val inst2ClientId: String,
    private val krrProxyClientId: String,
) {
    suspend fun pdlApiToken(accessToken: String): String {
        return tokendingsService.exchangeToken(accessToken, pdlApiClientId)
    }

    suspend fun pdlMottakToken(accessToken: String): String {
        return tokendingsService.exchangeToken(accessToken, pdlMottakClientId)
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

    suspend fun krrProxyToken(accessToken: String): String {
        return tokendingsService.exchangeToken(accessToken, krrProxyClientId)
    }
}
