package no.nav.modiacontextholder.metrics

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import no.nav.modiacontextholder.AppModule
import no.nav.modiacontextholder.config.Configuration
import no.nav.modiacontextholder.mock.mockModule
import no.nav.modiacontextholder.setupInfrastructure
import no.nav.modiacontextholder.utils.getAuthorizedParty
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Base64
import kotlin.jvm.optionals.getOrElse
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class MetricsTest {
    val audience = "testaudience"
    val issuer = "testissuer"

    @Test
    fun `authorized_party is available`() =
        testApplication {
            val generator = KeyPairGenerator.getInstance("RSA")
            generator.initialize(2048)
            val keyPair = generator.generateKeyPair()
            val provider = fakeJwkProvider("fakepovider", keyPair.public as RSAPublicKey)

            application {
                install(Koin) {
                    modules(
                        AppModule.appModule,
                        mockModule,
                        module {
                            single { Configuration() }
                        },
                    )
                }

                setupInfrastructure()

                install(Authentication) {
                    jwt("jwt-auth") {
                        verifier(provider, issuer) {
                            acceptLeeway(3)
                        }
                        validate { credential ->
                            if (credential.payload.getClaim("ident").asString() != "") {
                                JWTPrincipal(credential.payload)
                            } else {
                                null
                            }
                        }
                        challenge { _, _ ->
                            call.respond(HttpStatusCode.Unauthorized, "Invalid token")
                        }
                    }
                }

                routing {
                    authenticate("jwt-auth") {
                        get("/test") {
                            call.respondText(call.getAuthorizedParty().getOrElse { "no party" })
                        }
                    }
                    get("/unauthenticated") {
                        call.respondText("OK")
                    }
                }
            }

            val token = generateToken(keyPair.public as RSAPublicKey, keyPair.private as RSAPrivateKey)

            client.get("/test") { header("Authorization", "Bearer $token") }.apply {
                assertEquals("dev-gcp:aura:nais-testapp", this.bodyAsText())
            }

            client.get("/internal/metrics").apply {
                assertContains(
                    this.bodyAsText(),
                    """ktor_http_server_requests_seconds_count{address="localhost:80",authorized_party="dev-gcp:aura:nais-testapp",method="GET",route="/(authenticate jwt-auth)/test",status="200",throwable="n/a"} 1""",
                )
            }
            client.get("/unauthenticated").apply {
                this.bodyAsText()
            }
            client.get("/internal/metrics").apply {
                assertContains(
                    this.bodyAsText(),
                    """http_server_requests_seconds_count{address="localhost:80",authorized_party="",method="GET",route="/unauthenticated",status="200",throwable="n/a"} 1""",
                )
            }
            stopKoin()
        }

    private fun generateToken(
        publickey: RSAPublicKey,
        privatekey: RSAPrivateKey,
        azp_name: String = "dev-gcp:aura:nais-testapp",
    ) = JWT
        .create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("ident", "Z111111")
        .withClaim("azp_name", azp_name)
        .sign(Algorithm.RSA256(publickey, privatekey))

    private fun fakeJwkProvider(
        id: String,
        publicKey: RSAPublicKey,
    ): JwkProvider =

        JwkProvider {
            Jwk(
                id,
                "RSA",
                "RS256",
                "",
                listOf(),
                "",
                listOf(),
                "",
                mapOf(
                    "n" to Base64.getUrlEncoder().encodeToString(publicKey.modulus.toByteArray()),
                    "e" to Base64.getUrlEncoder().encodeToString(publicKey.publicExponent.toByteArray()),
                ),
            )
        }
}
