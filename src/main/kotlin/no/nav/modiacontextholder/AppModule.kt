package no.nav.modiacontextholder

import io.getunleash.UnleashContextProvider
import io.ktor.http.*
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import no.nav.common.client.axsys.AxsysClient
import no.nav.common.client.axsys.AxsysV2ClientImpl
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
import no.nav.modiacontextholder.mock.MockNomClient
import no.nav.modiacontextholder.redis.RedisPublisher
import no.nav.modiacontextholder.redis.RedisVeilederContextDatabase
import no.nav.modiacontextholder.redis.VeilederContextDatabase
import no.nav.modiacontextholder.service.*
import no.nav.modiacontextholder.service.unleash.ToggleableFeatureService
import no.nav.modiacontextholder.service.unleash.UnleashContextProviderImpl
import no.nav.modiacontextholder.service.unleash.UnleashService
import no.nav.modiacontextholder.utils.*
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.dsl.onClose

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
            } onClose { it?.close() }

            single<StatefulRedisConnection<String, String>> {
                val redisClient = get<RedisClient>()
                redisClient.connect()
            } onClose { it?.close() }

            single<StatefulRedisPubSubConnection<String, String>> {
                val redisClient = get<RedisClient>()
                redisClient.connectPubSub()
            } onClose { it?.close() }

            singleOf(::RedisVeilederContextDatabase) { bind<VeilederContextDatabase>() }
            single { RedisPublisher(get()) }

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

            singleOf(::EnheterCache)
            singleOf(::EnheterService)
        }
    val externalModules =
        module {
            singleOf(::PdlServiceImpl) { bind<PdlService>() }

            single {
                CachedMsGraphClient(
                    MsGraphHttpClient(EnvironmentUtils.getRequiredProperty("MS_GRAPH_URL")),
                )
            }

            single<AzureADService> {
                val oboflowTokenProvider: OnBehalfOfTokenClient = get()
                AzureADServiceImpl(
                    graphUrl = Url(EnvironmentUtils.getRequiredProperty("MS_GRAPH_URL")),
                    tokenClient = oboflowTokenProvider.bindTo(EnvironmentUtils.getRequiredProperty("MS_GRAPH_SCOPE")),
                )
            }

            single<AxsysClient> {
                val machineToMachineTokenProvider: MachineToMachineTokenClient = get()
                val httpClient: OkHttpClient =
                    RestClient
                        .baseClient()
                        .newBuilder()
                        .addInterceptor(
                            LoggingInterceptor("Axsys") {
                                getCallId()
                            },
                        ).build()
                val downstreamApi = DownstreamApi.parse(EnvironmentUtils.getRequiredProperty("AXSYS_SCOPE"))
                val tokenSupplier = {
                    machineToMachineTokenProvider.createMachineToMachineToken(downstreamApi)
                }

                AxsysV2ClientImpl(
                    EnvironmentUtils.getRequiredProperty("AXSYS_URL"),
                    tokenSupplier,
                    httpClient,
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
            }
        }
}
