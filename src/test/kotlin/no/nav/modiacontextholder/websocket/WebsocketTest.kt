package no.nav.modiacontextholder.websocket

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import kotlinx.coroutines.withTimeout
import no.nav.modiacontextholder.config.Configuration
import no.nav.modiacontextholder.domain.VeilederContextType
import no.nav.modiacontextholder.modiacontextholderApp
import no.nav.modiacontextholder.valkey.TestUtils
import no.nav.modiacontextholder.rest.model.RSNyContext
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import kotlin.test.*

class WebsocketTest : TestUtils.WithValkey() {
    private val hostAndPort = redisHostAndPort()
    private val redisUri = "redis://default:$PASSWORD@$hostAndPort"
    val audience = "testaudience"
    val issuer = "testissuer"

    @Test
    fun `Websockets are sent on context update`() {
        testApplication {
            val client =
                createClient {
                    install(WebSockets)
                    install(ContentNegotiation) {
                        json()
                    }
                }

            application {
                modiacontextholderApp(configuration = Configuration(redisUri = redisUri, isMock = true), useMock = true)
            }

            withTimeout(5000) {
                client.webSocket("/ws/Z999999") {
                    val generator = KeyPairGenerator.getInstance("RSA")
                    generator.initialize(2048)
                    val keyPair = generator.generateKeyPair()
                    val nyContext = RSNyContext("10108000398", VeilederContextType.NY_AKTIV_BRUKER)
                    val token = generateToken(keyPair.public as RSAPublicKey, keyPair.private as RSAPrivateKey)
                    client.post("/api/context") {
                        contentType(ContentType.Application.Json)
                        setBody(nyContext)
                        header("Authorization", "Bearer $token")
                    }

                    val frame = incoming.receive() as Frame.Text
                    val value = frame.readText()
                    assertEquals(value, VeilederContextType.NY_AKTIV_BRUKER.name)
                    close()
                }
            }
            closeSubscriber()
        }
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
}
