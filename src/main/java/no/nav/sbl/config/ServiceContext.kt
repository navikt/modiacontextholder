package no.nav.sbl.config

import io.ktor.http.Url
import no.nav.common.client.axsys.AxsysClient
import no.nav.common.client.axsys.AxsysV2ClientImpl
import no.nav.common.client.msgraph.CachedMsGraphClient
import no.nav.common.client.msgraph.MsGraphClient
import no.nav.common.client.msgraph.MsGraphHttpClient
import no.nav.common.client.nom.NomClient
import no.nav.common.rest.client.RestClient
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.common.token_client.client.OnBehalfOfTokenClient
import no.nav.common.utils.EnvironmentUtils
import no.nav.sbl.azure.AzureADServiceImpl
import no.nav.sbl.consumers.norg2.Norg2Client
import no.nav.sbl.redis.RedisPublisher
import no.nav.sbl.redis.VeilederContextDatabase
import no.nav.sbl.service.AuthContextService
import no.nav.sbl.service.ContextService
import no.nav.sbl.service.EnheterCache
import no.nav.sbl.service.EnheterService
import no.nav.sbl.service.PdlService
import no.nav.sbl.service.VeilederService
import no.nav.sbl.util.DownstreamApi
import no.nav.sbl.util.LoggingInterceptor
import no.nav.sbl.util.XCorrelationIdInterceptor
import no.nav.sbl.util.bindTo
import no.nav.sbl.util.createMachineToMachineToken
import no.nav.sbl.util.getCallId
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(FeatureToggleConfig::class, RedisConfig::class)
open class ServiceContext {
    @Bean
    open fun contextService(
        veilederContextDatabase: VeilederContextDatabase,
        redisPublisher: RedisPublisher,
    ) = ContextService(veilederContextDatabase, redisPublisher)

    @Bean
    open fun enheterCache(norg2Client: Norg2Client) = EnheterCache(norg2Client)

    @Bean
    open fun veilederCache(nomClient: NomClient) = VeilederService(nomClient)

    @Bean
    open fun enhetService(
        client: AxsysClient,
        enheterCache: EnheterCache,
    ) = EnheterService(client, enheterCache)

    @Bean
    open fun machineToMachineTokenProvider(): MachineToMachineTokenClient =
        AzureAdTokenClientBuilder
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
        PdlService(machineToMachineTokenProvider.bindTo(DownstreamApi.parse(EnvironmentUtils.getRequiredProperty("PDL_SCOPE"))))

    @Bean
    open fun msGraphClient() =
        CachedMsGraphClient(
            MsGraphHttpClient(EnvironmentUtils.getRequiredProperty("MS_GRAPH_URL")),
        )

    @Bean
    open fun azureADService(oboflowTokenProvider: OnBehalfOfTokenClient) =
        AzureADServiceImpl(
            graphUrl = Url(EnvironmentUtils.getRequiredProperty("MS_GRAPH_URL")),
            tokenClient = oboflowTokenProvider.bindTo(EnvironmentUtils.getRequiredProperty("MS_GRAPH_SCOPE")),
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
                ).build()
        val downstreamApi = DownstreamApi.parse(EnvironmentUtils.getRequiredProperty("AXSYS_SCOPE"))
        val tokenSupplier = {
            machineToMachineTokenProvider.createMachineToMachineToken(downstreamApi)
        }

        return AxsysV2ClientImpl(
            EnvironmentUtils.getRequiredProperty("AXSYS_URL"),
            tokenSupplier,
            httpClient,
        )
    }

    @Bean
    open fun norg2Client(): Norg2Client? {
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

        return Norg2Client(EnvironmentUtils.getRequiredProperty("NORG2_API_URL"), client)
    }

    @Bean
    open fun authContextService(msGraphClient: MsGraphClient) = AuthContextService(msGraphClient)
}
