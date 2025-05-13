package no.nav.tms.personopplysninger.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.utils.io.*
import no.nav.tms.token.support.idporten.sidecar.mock.idPortenMock
import no.nav.tms.token.support.tokenx.validation.mock.tokenXMock
import no.nav.tms.token.support.tokenx.validation.mock.LevelOfAssurance as TokenXLoa
import no.nav.tms.token.support.idporten.sidecar.mock.LevelOfAssurance as IdPortenLoa
import java.text.DateFormat

abstract class RouteTest {

    val testIdent = "01234567890"

    private val objectMapper = jacksonObjectMapper()

    @KtorDsl
    fun apiTest(
        internalRouteConfig: (HttpClient) -> (Route.() -> Unit),
        userIdent: String = testIdent,
        userLoa: UserLoa = UserLoa.High,
        block: suspend ApplicationTestBuilder.(HttpClient) -> Unit
    ) = testApplication {

        val serverClient = client.config {
            install(ContentNegotiation) {
                jackson {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    registerModule(JavaTimeModule())
                    dateFormat = DateFormat.getDateTimeInstance()
                }
            }
        }

        application {
            mainModule(
                internalRouteConfig(serverClient),
                serverClient,
                authInstaller = {
                    authentication {
                        idPortenMock {
                            setAsDefault = true
                            alwaysAuthenticated = true
                            staticUserPid = userIdent
                            staticLevelOfAssurance = userLoa.toIdPortenLoa()
                        }
                        tokenXMock {
                            setAsDefault = false
                            alwaysAuthenticated = true
                            staticUserPid = userIdent
                            staticLevelOfAssurance = userLoa.toTokenXLoa()
                        }
                    }
                }
            )
        }

        this.block(
            client.config {
                install(ContentNegotiation) {
                    jackson {
                        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        registerModule(JavaTimeModule())
                        dateFormat = DateFormat.getDateTimeInstance()
                    }
                }
            }
        )
    }

    fun ApplicationTestBuilder.externalService(host: String, route: Route.() -> Unit) {
        externalServices {
            hosts(host) {
                install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                    jackson {
                        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        registerModule(JavaTimeModule())
                        dateFormat = DateFormat.getDateTimeInstance()
                    }
                }
                routing(route)
            }
        }
    }

    suspend fun HttpResponse.json() = bodyAsText().let { objectMapper.readTree(it) }
    suspend fun ApplicationCall.receiveJson() = receiveText().let { objectMapper.readTree(it) }

    fun JsonNode.asTextOrNull() = if(isNull) null else asText()

    enum class UserLoa {
        Substantial, High;

        fun toIdPortenLoa() = if (this == High) IdPortenLoa.HIGH else IdPortenLoa.SUBSTANTIAL

        fun toTokenXLoa() = if (this == High) TokenXLoa.HIGH else TokenXLoa.SUBSTANTIAL
    }
}

typealias InternalRouteConfig = (HttpClient) -> (Route.() -> Unit)

@KtorDsl
fun routeConfig(block: Route.() -> Unit) = block
