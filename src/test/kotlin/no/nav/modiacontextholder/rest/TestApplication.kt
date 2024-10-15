package no.nav.modiacontextholder.rest

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import no.nav.modiacontextholder.config.Configuration
import no.nav.modiacontextholder.modiacontextholderApp
import no.nav.modiacontextholder.redis.TestUtils
import no.nav.modiacontextholder.redis.TestUtils.WithRedis.Companion.PASSWORD
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.koin.test.KoinTest

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

    @BeforeEach
    fun beforeEach() {
        this.configuration =
            Configuration(
                redisUri = redisUri,
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
