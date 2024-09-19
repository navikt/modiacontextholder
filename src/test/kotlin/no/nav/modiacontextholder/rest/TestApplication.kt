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
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import org.koin.test.junit5.mock.MockProviderExtension

open class TestApplication : KoinTest {
    var configuration = Configuration()

    fun testApp(block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) =
        testApplication {
            application {
                modiacontextholderApp(configuration = configuration, useMock = true)
            }
            startApplication()
            val client =
                createClient {
                    install(ContentNegotiation) { json() }
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
