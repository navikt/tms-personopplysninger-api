package no.nav.tms.personopplysninger.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.statement.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json
import no.nav.tms.token.support.idporten.sidecar.mock.idPortenMock
import no.nav.tms.token.support.tokenx.validation.mock.tokenXMock
import no.nav.tms.token.support.tokenx.validation.mock.LevelOfAssurance as TokenXLoa
import no.nav.tms.token.support.idporten.sidecar.mock.LevelOfAssurance as IdPortenLoa
import java.text.DateFormat

abstract class ApiTest {
    val ident = "01234567890"

    private val objectMapper = jacksonObjectMapper()

    @KtorDsl
    fun setupApi(
        userIdent: String = ident,
        userLoa: UserLoa = UserLoa.High,
        internalRouteConfig: (HttpClient) -> (Route.() -> Unit)
    ) = TestConf (
        userIdent = userIdent,
        userLoa = userLoa,
        internalRouteConfig = internalRouteConfig
    )

    class TestConf(
        val userIdent: String,
        val userLoa: UserLoa,
        val internalRouteConfig: InternalRouteConfig
    ) {
        @KtorDsl
        fun runTest(
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
    }

    fun ApplicationTestBuilder.externalService(host: String, route: RouteConfig) {
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

    enum class UserLoa {
        Substantial, High;

        fun toIdPortenLoa() = if (this == High) IdPortenLoa.HIGH else IdPortenLoa.SUBSTANTIAL

        fun toTokenXLoa() = if (this == High) TokenXLoa.HIGH else TokenXLoa.SUBSTANTIAL
    }
}

typealias InternalRouteConfig = (HttpClient) -> RouteConfig
typealias RouteConfig = Route.() -> Unit

