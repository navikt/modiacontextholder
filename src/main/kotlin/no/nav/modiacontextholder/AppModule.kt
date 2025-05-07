package no.nav.modiacontextholder

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.UnleashContextProvider
import io.getunleash.util.UnleashConfig
import io.ktor.http.*
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import no.nav.common.client.msgraph.CachedMsGraphClient
import no.nav.common.client.msgraph.MsGraphHttpClient
import no.nav.common.client.nom.NomClient
import no.nav.common.client.nom.NomClientImpl
import no.nav.common.rest.client.RestClient
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.common.token_client.client.OnBehalfOfTokenClient
import no.nav.common.utils.EnvironmentUtils
import no.nav.modiacontextholder.config.Configuration
import no.nav.modiacontextholder.consumers.norg2.Norg2Client
import no.nav.modiacontextholder.consumers.norg2.Norg2ClientImpl
import no.nav.modiacontextholder.infrastructur.HealthCheckAware
import no.nav.modiacontextholder.mock.MockNomClient
import no.nav.modiacontextholder.valkey.ValkeyPersistence
import no.nav.modiacontextholder.valkey.ValkeyPublisher
import no.nav.modiacontextholder.valkey.ValkeyVeilederContextDatabase
import no.nav.modiacontextholder.valkey.VeilederContextDatabase
import no.nav.modiacontextholder.service.*
import no.nav.modiacontextholder.service.unleash.ToggleableFeatureService
import no.nav.modiacontextholder.service.unleash.UnleashContextProviderImpl
import no.nav.modiacontextholder.service.unleash.UnleashService
import no.nav.modiacontextholder.utils.*
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.binds
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose

const val REDIS_CACHE_DB = 1

object AppModule {
    val appModule =
        module {
            singleOf(::UnleashContextProviderImpl) { bind<UnleashContextProvider>() }
            singleOf(::UnleashService) { bind<ToggleableFeatureService>() }

            single<RedisClient> {
                val configuration = get<Configuration>()
                val baseUri = RedisURI.create(configuration.redisUri)
                val uri =
                    if (configuration.redisUsername.isNullOrEmpty() && configuration.redisPassword.isNullOrEmpty()) {
                        baseUri
                    } else {
                        RedisURI
                            .builder(baseUri)
                            .withHost(baseUri.host)
                            .withPort(baseUri.port)
                            .withAuthentication(configuration.redisUsername, configuration.redisPassword)
                            .build()
                    }
                RedisClient.create(uri)
            } onClose {
                it?.close()
            }

            single<RedisClient>(named("cache")) {
                val configuration = get<Configuration>()
                val baseUri = RedisURI.create(configuration.redisUri)
                val uri =
                    if (configuration.redisUsername.isNullOrEmpty() && configuration.redisPassword.isNullOrEmpty()) {
                        RedisURI.builder(baseUri).withDatabase(REDIS_CACHE_DB).build()
                    } else {
                        RedisURI
                            .builder(baseUri)
                            .withHost(baseUri.host)
                            .withPort(baseUri.port)
                            .withDatabase(REDIS_CACHE_DB)
                            .withAuthentication(configuration.redisUsername, configuration.redisPassword)
                            .build()
                    }
                RedisClient.create(uri)
            } onClose {
                it?.close()
            }

            single<StatefulRedisConnection<String, String>> {
                val redisClient = get<RedisClient>()
                redisClient.connect()
            } onClose { it?.close() }

            single<StatefulRedisPubSubConnection<String, String>> {
                val redisClient = get<RedisClient>()
                redisClient.connectPubSub()
            } onClose { it?.close() }

            singleOf(::ValkeyVeilederContextDatabase) { binds(listOf(VeilederContextDatabase::class, HealthCheckAware::class)) }
            single { ValkeyPublisher(get()) }
            singleOf(::VeilederService)
            singleOf(::ContextService)

            single<MachineToMachineTokenClient> {
                AzureAdTokenClientBuilder
                    .builder()
                    .withNaisDefaults()
                    .buildMachineToMachineTokenClient()
            }

            single<OnBehalfOfTokenClient> {
                AzureAdTokenClientBuilder
                    .builder()
                    .withNaisDefaults()
                    .buildOnBehalfOfTokenClient()
            }

            singleOf(::EnheterCache) { bind<HealthCheckAware>() }
            single { EnheterService(get(), get()) }
            single { ValkeyPersistence(get()) }
            singleOf(::FnrCodeExchangeService)
        }
    val externalModules =
        module {
            single<Unleash> {
                val api: String = EnvironmentUtils.getRequiredProperty("UNLEASH_SERVER_API_URL") + "/api"
                val apiToken: String = EnvironmentUtils.getRequiredProperty("UNLEASH_SERVER_API_TOKEN")

                val unleashConfig =
                    UnleashConfig
                        .builder()
                        .apply {
                            appName("modiacontextholder")
                            environment(System.getProperty("UNLEASH_ENVIRONMENT"))
                            instanceId(System.getProperty("APP_ENVIRONMENT_NAME", "local"))
                            unleashAPI(api)
                            apiKey(apiToken)
                            unleashContextProvider(get())
                            synchronousFetchOnInitialisation(true)
                        }.build()

                DefaultUnleash(unleashConfig)
            }
            singleOf(::PdlServiceImpl) { bind<PdlService>() }

            single {
                CachedMsGraphClient(
                    MsGraphHttpClient(EnvironmentUtils.getRequiredProperty("MS_GRAPH_URL")),
                )
            }

            single<AzureADService> {
                val oboflowTokenProvider: OnBehalfOfTokenClient = get()
                AzureADServiceImpl(
                    httpClient =
                        RestClient
                            .baseClient()
                            .newBuilder()
                            .addInterceptor(
                                LoggingInterceptor("AzureAD") {
                                    getCallId()
                                },
                            ).build(),
                    graphUrl = Url(EnvironmentUtils.getRequiredProperty("MS_GRAPH_URL")),
                    tokenClient = oboflowTokenProvider.bindTo(EnvironmentUtils.getRequiredProperty("MS_GRAPH_SCOPE")),
                )
            }

            single<NomClient> {
                val machineToMachineTokenProvider: MachineToMachineTokenClient = get()
                val httpClient: OkHttpClient =
                    RestClient
                        .baseClient()
                        .newBuilder()
                        .addInterceptor(
                            LoggingInterceptor("Nom") {
                                getCallId()
                            },
                        ).build()
                if (EnvironmentUtils.isDevelopment().orElse(false)) {
                    MockNomClient()
                } else {

                    val downstreamApi = DownstreamApi.parse(EnvironmentUtils.getRequiredProperty("NOM_SCOPE"))
                    val url = EnvironmentUtils.getRequiredProperty("NOM_URL")

                    val tokenSupplier = {
                        machineToMachineTokenProvider.createMachineToMachineToken(downstreamApi)
                    }

                    NomClientImpl(url, tokenSupplier, httpClient)
                }
            }

            single<Norg2Client> {
                val client: OkHttpClient =
                    RestClient
                        .baseClient()
                        .newBuilder()
                        .addInterceptor(XCorrelationIdInterceptor())
                        .addInterceptor(
                            LoggingInterceptor("Norg2") { request ->
                                requireNotNull(request.header("X-Correlation-ID")) {
                                    "Kall uten \"X-Correlation-ID\" er ikke lov"
                                }
                            },
                        ).build()

                Norg2ClientImpl(EnvironmentUtils.getRequiredProperty("NORG2_API_URL"), client)
            } withOptions {
                binds(listOf(HealthCheckAware::class))
            }
        }
}
