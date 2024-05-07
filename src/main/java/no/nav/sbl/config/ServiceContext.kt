package no.nav.sbl.config

import io.ktor.http.*
import no.nav.common.client.msgraph.CachedMsGraphClient
import no.nav.common.client.msgraph.MsGraphClient
import no.nav.common.client.msgraph.MsGraphHttpClient
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.common.token_client.client.OnBehalfOfTokenClient
import no.nav.common.utils.EnvironmentUtils
import no.nav.sbl.azure.AzureADServiceImpl
import no.nav.sbl.db.DatabaseCleanerService
import no.nav.sbl.db.dao.EventDAO
import no.nav.sbl.redis.RedisConfig
import no.nav.sbl.redis.RedisPublisher
import no.nav.sbl.service.*
import no.nav.sbl.util.DownstreamApi.Companion.parse
import no.nav.sbl.util.bindTo
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(FeatureToggleConfig::class, AxsysConfig::class, RedisConfig::class)
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
    open fun enhetService() = EnheterService()

    @Bean
    open fun machineToMachineTokenProvider() = AzureAdTokenClientBuilder
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
    open fun authContextService(msGraphClient: MsGraphClient?) = AuthContextService(msGraphClient)
}
