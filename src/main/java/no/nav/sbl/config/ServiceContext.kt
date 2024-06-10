package no.nav.sbl.config

import io.ktor.http.*
import no.nav.common.client.axsys.AxsysClient
import no.nav.common.client.axsys.AxsysV2ClientImpl
import no.nav.common.client.axsys.CachedAxsysClient
import no.nav.common.client.msgraph.CachedMsGraphClient
import no.nav.common.client.msgraph.MsGraphClient
import no.nav.common.client.msgraph.MsGraphHttpClient
import no.nav.common.rest.client.RestClient
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.common.token_client.client.OnBehalfOfTokenClient
import no.nav.common.utils.EnvironmentUtils
import no.nav.sbl.azure.AzureADServiceImpl
import no.nav.sbl.consumers.norg2.Norg2Client
import no.nav.sbl.db.DatabaseCleanerService
import no.nav.sbl.db.dao.EventDAO
import no.nav.sbl.redis.RedisConfig
import no.nav.sbl.redis.RedisPublisher
import no.nav.sbl.service.*
import no.nav.sbl.util.DownstreamApi.Companion.parse
import no.nav.sbl.util.bindTo
import no.nav.sbl.util.createMachineToMachineToken
import no.nav.utils.LoggingInterceptor
import no.nav.utils.XCorrelationIdInterceptor
import no.nav.utils.getCallId
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(FeatureToggleConfig::class, RedisConfig::class)
open class ServiceContext {

    @Bean
    open fun contextService(eventDAO: EventDAO?, redisPublisher: RedisPublisher?) =
        ContextService(eventDAO, redisPublisher)

    @Bean
    open fun eventService() = EventService()

    @Bean
    open fun databaseCleanerService() = DatabaseCleanerService()

    @Bean
    open fun enheterCache() = EnheterCache()

    @Bean
    open fun veilederCache() = VeilederService()

    @Bean
    open fun enhetService(
        client: AxsysClient,
        enheterCache: EnheterCache,
    ) = EnheterService(client, enheterCache)

    @Bean
    open fun machineToMachineTokenProvider(): MachineToMachineTokenClient = AzureAdTokenClientBuilder
        .builder()
        .withNaisDefaults()
        .buildMachineToMachineTokenClient()

    @Bean
    open fun oboflowTokenProvider(): OnBehalfOfTokenClient =
        AzureAdTokenClientBuilder
            .builder()
            .withNaisDefaults()
            .buildOnBehalfOfTokenClient()

    @Bean
    open fun pdlService(machineToMachineTokenProvider: MachineToMachineTokenClient) =
        PdlService(machineToMachineTokenProvider.bindTo(parse(EnvironmentUtils.getRequiredProperty("PDL_SCOPE"))))


    @Bean
    open fun msGraphClient() = CachedMsGraphClient(
        MsGraphHttpClient(EnvironmentUtils.getRequiredProperty("MS_GRAPH_URL"))
    )

    @Bean
    open fun azureADService(oboflowTokenProvider: OnBehalfOfTokenClient) = AzureADServiceImpl(
        graphUrl = Url(EnvironmentUtils.getRequiredProperty("MS_GRAPH_URL")),
        tokenClient = oboflowTokenProvider.bindTo(EnvironmentUtils.getRequiredProperty("MS_GRAPH_SCOPE"))
    )

    @Bean
    open fun axsysClient(machineToMachineTokenProvider: MachineToMachineTokenClient): AxsysClient {
        val httpClient: OkHttpClient =
            RestClient
                .baseClient()
                .newBuilder()
                .addInterceptor(
                    LoggingInterceptor("Axsys") {
                        getCallId()
                    },
                )
                .build()
        val downstreamApi = parse(EnvironmentUtils.getRequiredProperty("AXSYS_SCOPE"))
        val tokenSupplier = {
            machineToMachineTokenProvider.createMachineToMachineToken(downstreamApi)
        }

        return CachedAxsysClient(
            AxsysV2ClientImpl(
                EnvironmentUtils.getRequiredProperty("AXSYS_URL"),
                tokenSupplier,
                httpClient
            )
        )
    }

    @Bean
    open fun norg2Client(): Norg2Client? {
        val client: OkHttpClient =
            RestClient.baseClient().newBuilder()
                .addInterceptor(XCorrelationIdInterceptor())
                .addInterceptor(
                    LoggingInterceptor("Norg2") { request ->
                        requireNotNull(request.header("X-Correlation-ID")) {
                            "Kall uten \"X-Correlation-ID\" er ikke lov"
                        }
                    },
                )
                .build()

        return Norg2Client(EnvironmentUtils.getRequiredProperty("NORG2_API_URL"), client)
    }

    @Bean
    open fun authContextService(msGraphClient: MsGraphClient?) = AuthContextService(msGraphClient)
}
