package no.nav.tms.personopplysninger.api.common;

import no.nav.tms.token.support.user.token.exchange.UserTokenExchanger

class TokenExchanger(
    private val userTokenExchanger: UserTokenExchanger,
    private val kontoregisterClientId: String,
    private val pdlApiClientId: String,
    private val pdlMottakClientId: String,
    private val medlClientId: String,
    private val inst2ClientId: String,
    private val krrProxyClientId: String,
    private val sporingsloggClientId: String,
) {
    suspend fun pdlApiToken(accessToken: String): String {
        return userTokenExchanger.exchangeToken(accessToken, pdlApiClientId)
    }

    suspend fun pdlMottakToken(accessToken: String): String {
        return userTokenExchanger.exchangeToken(accessToken, pdlMottakClientId)
    }

    suspend fun kontoregisterToken(accessToken: String): String {
        return userTokenExchanger.exchangeToken(accessToken, kontoregisterClientId)
    }

    suspend fun medlToken(accessToken: String): String {
        return userTokenExchanger.exchangeToken(accessToken, medlClientId)
    }

    suspend fun inst2Token(accessToken: String): String {
        return userTokenExchanger.exchangeToken(accessToken, inst2ClientId)
    }

    suspend fun krrProxyToken(accessToken: String): String {
        return userTokenExchanger.exchangeToken(accessToken, krrProxyClientId)
    }

    suspend fun sporingsloggToken(accessToken: String): String {
        return userTokenExchanger.exchangeToken(accessToken, sporingsloggClientId)
    }
}
