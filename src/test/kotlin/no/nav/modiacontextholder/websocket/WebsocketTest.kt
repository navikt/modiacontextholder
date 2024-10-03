package no.nav.modiacontextholder.websocket

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
import no.nav.modiacontextholder.redis.TestUtils
import no.nav.modiacontextholder.rest.model.RSNyContext
import kotlin.test.*

class WebsocketTest : TestUtils.WithRedis() {
    private val hostAndPort = redisHostAndPort()
    private val redisUri = "redis://default:$PASSWORD@$hostAndPort"

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
                    val nyContext = RSNyContext("10108000398", VeilederContextType.NY_AKTIV_BRUKER)
                    client.post("/api/context") {
                        contentType(ContentType.Application.Json)
                        setBody(nyContext)
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
}
