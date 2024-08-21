package no.nav.sbl.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.pubsub.RedisPubSubListener
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import no.nav.sbl.config.ApplicationCluster
import no.nav.sbl.config.WebSocketConfiguration
import no.nav.sbl.rest.ContextRessurs
import no.nav.sbl.rest.model.RSContext
import no.nav.sbl.rest.model.RSNyContext
import no.nav.sbl.service.AuthContextService
import no.nav.sbl.service.ContextService
import no.nav.sbl.websocket.ClientWebSocketHandler
import no.nav.sbl.websocket.ContextWebSocketHandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(
    classes = [WebSocketConfiguration::class, RedisPersistenceAndPubSubTest.RedisPersistenceAndPubSubTestConfiguration::class],
)
@EnableAutoConfiguration
@AutoConfigureMockMvc
@Testcontainers
class RedisPersistenceAndPubSubTest {
    companion object {
        @Container
        private val redisContainer = TestUtils.RedisContainer()

        const val IDENT = "ident1"
        const val REDIS_CHANNEL = "test-channel"

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            mockkObject(ApplicationCluster)
            every { ApplicationCluster.isFss() } returns false
            every { ApplicationCluster.isGcp() } returns true
        }
    }

    @LocalServerPort
    var port: Int = 0

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var authContextService: AuthContextService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `jedis pool håndterer både persistence og pubsub`(): Unit =
        runBlocking {
            every { authContextService.ident } returns Optional.of(IDENT)

            val websocketChannel = Channel<String>()
            val webSocketClient = StandardWebSocketClient()

            val clientWebSocketHandler = ClientWebSocketHandler(websocketChannel)

            val session =
                webSocketClient
                    .execute(
                        clientWebSocketHandler,
                        "ws://localhost:$port/ws/$IDENT",
                    ).get()

            mockMvc
                .post("/api/context") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(RSNyContext("aktivEnhet", "NY_AKTIV_ENHET"))
                }.andExpect {
                    status { isOk() }
                }

            val message = websocketChannel.receive()
            // language=JSON
            assertThat(message).isEqualTo("""{"veilederIdent":"ident1","eventType":"NY_AKTIV_ENHET"}""")

            mockMvc
                .get("/api/context")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { json(objectMapper.writeValueAsString(RSContext(null, "aktivEnhet"))) }
                }

            session.close()
        }

    @Configuration
    open class RedisPersistenceAndPubSubTestConfiguration {
        @Bean
        open fun redisClient(): RedisClient =
            RedisClient.create(
                RedisURI
                    .builder(RedisURI.create("redis://${redisContainer.host}:${redisContainer.getMappedPort(6379)}"))
                    .withAuthentication("default", "password")
                    .build(),
            )

        @Bean
        open fun statefulRedisConnection(redisClient: RedisClient): StatefulRedisConnection<String, String> = redisClient.connect()

        @Bean
        open fun statefulRedisPubSubConnection(
            redisClient: RedisClient,
            redisPubSubListeners: List<RedisPubSubListener<String, String>>,
        ): StatefulRedisPubSubConnection<String, String> =
            redisClient.connectPubSub().apply {
                redisPubSubListeners.forEach(this::addListener)
            }

        @Bean("redisCommands")
        open fun redisCommands(statefulRedisConnection: StatefulRedisConnection<String, String>): RedisCommands<String, String> =
            statefulRedisConnection.sync()

        @Bean("redisPubSubCommands")
        open fun redisPubSubCommands(
            statefulRedisPubSubConnection: StatefulRedisPubSubConnection<String, String>,
            redisSubscriptions: List<RedisSubscription>,
        ): RedisPubSubCommands<String, String> =
            statefulRedisPubSubConnection.sync().apply {
                redisSubscriptions.forEach {
                    subscribe(it.channel)
                }
            }

        @Bean
        open fun redisSubscription(handler: ContextWebSocketHandler): RedisSubscription =
            RedisSubscription(REDIS_CHANNEL) { _, message ->
                LoggerFactory.getLogger(RedisSubscription::class.java).info("Received message $message")
                handler.publishMessage(IDENT, message)
            }

        @Bean
        open fun authContextService(): AuthContextService = mockk()

        @Bean
        open fun objectMapper(): ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

        @Bean
        open fun veilederContextDatabase(
            @Qualifier("redisCommands") redis: RedisCommands<String, String>,
            objectMapper: ObjectMapper,
        ): VeilederContextDatabase = RedisVeilederContextDatabase(redis, objectMapper)

        @Bean
        open fun redisPublisher(redisPubSub: RedisPubSubCommands<String, String>): RedisPublisher =
            RedisPublisher(redisPubSub, "test-channel")

        @Bean
        open fun contextService(
            veilederContextDatabase: VeilederContextDatabase,
            redisPublisher: RedisPublisher,
        ): ContextService = ContextService(veilederContextDatabase, redisPublisher, mockk(), mockk())

        @Bean
        open fun contextRessurs(
            contextService: ContextService,
            authContextService: AuthContextService,
        ): ContextRessurs = ContextRessurs(contextService, authContextService)
    }
}
