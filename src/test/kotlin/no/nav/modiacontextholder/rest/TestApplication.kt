package no.nav.modiacontextholder.rest

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import io.mockk.mockkClass
import no.nav.modiacontextholder.AppModule
import no.nav.modiacontextholder.config.Configuration
import no.nav.modiacontextholder.mock.MockAzureADService
import no.nav.modiacontextholder.modiacontextholderApp
import no.nav.modiacontextholder.service.AzureADService
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import org.koin.test.junit5.mock.MockProviderExtension

open class TestApplication : KoinTest {
    fun testApp(block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) =
        testApplication {
            application {
                modiacontextholderApp(Configuration(), true)
            }
            startApplication()
            loadKoinModules(
                module {
                    single<AzureADService> { MockAzureADService() }
                },
            )
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
}
