package no.nav.modiacontextholder.rest

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import io.mockk.mockkClass
import no.nav.modiacontextholder.AppModule
import no.nav.modiacontextholder.config.Configuration
import no.nav.modiacontextholder.modiacontextholderApp
import no.nav.modiacontextholder.redis.TestUtils
import no.nav.modiacontextholder.redis.TestUtils.WithRedis.Companion.PASSWORD
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import org.koin.test.junit5.mock.MockProviderExtension

open class TestApplication : KoinTest {
    companion object {
        val withRedis = TestUtils.WithRedis()

        @JvmStatic
        @BeforeAll
        fun setup() {
            TestUtils.WithRedis.startContainer()
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            TestUtils.WithRedis.stopContainer()
        }
    }

    private val hostAndPort = withRedis.redisHostAndPort()
    private val redisUri = "redis://default:$PASSWORD@$hostAndPort"
    private val redisCacheUri = "redis://default:$PASSWORD@$hostAndPort/1"

    @BeforeEach
    fun beforeEach() {
        this.configuration =
            Configuration(
                redisUri = redisUri,
                redisCacheUri = redisCacheUri,
                aaRegisteretBaseUrl = "http://aaregisteret.local",
                aaRegisteretPublicUrl = "http://aaregistert-public.local",
                salesforceBaseUrl = "http://salesforce.local",
            )
    }

    var configuration =
        Configuration(
            aaRegisteretBaseUrl = "http://aaregisteret.local",
            aaRegisteretPublicUrl = "http://aaregistert-public.local",
            salesforceBaseUrl = "http://salesforce.local",
        )

    fun testApp(
        additionalSetupBlock: suspend ApplicationTestBuilder.() -> Unit = {},
        block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit,
    ) = testApplication {
        additionalSetupBlock()
        application {
            modiacontextholderApp(configuration = configuration, useMock = true)
        }
        startApplication()
        val client =
            createClient {
                install(ContentNegotiation) { json() }
                followRedirects = false
            }

        block(client)
    }

    @JvmField
    @RegisterExtension
    val koinTestExtension =
        KoinTestExtension.create {
            modules(
                AppModule.appModule,
            )
        }

    @JvmField
    @RegisterExtension
    val mockProvider =
        MockProviderExtension.create { clazz ->
            mockkClass(clazz)
        }

    suspend fun HttpClient.getAuth(url: String) = this.get(url) { header("Authorization", "Bearer token") }

    suspend inline fun <reified T> HttpClient.postAuth(
        url: String,
        body: T,
    ) = this.post(url) {
        header("Authorization", "Bearer token")
        setBody(body)
        contentType(ContentType.Application.Json)
    }
}
