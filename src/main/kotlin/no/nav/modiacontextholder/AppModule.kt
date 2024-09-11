package no.nav.modiacontextholder

import io.ktor.http.*
import no.nav.common.client.axsys.AxsysV2ClientImpl
import no.nav.common.client.msgraph.CachedMsGraphClient
import no.nav.common.client.msgraph.MsGraphHttpClient
import no.nav.common.rest.client.RestClient
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.common.token_client.client.OnBehalfOfTokenClient
import no.nav.common.utils.EnvironmentUtils
import no.nav.modiacontextholder.service.ContextService
import no.nav.modiacontextholder.service.PdlService
import no.nav.modiacontextholder.service.VeilederService
import no.nav.modiacontextholder.service.unleash.ToggleableFeatureService
import no.nav.modiacontextholder.service.unleash.UnleashService
import no.nav.modiacontextholder.utils.*
import no.nav.sbl.azure.AzureADServiceImpl
import no.nav.sbl.consumers.norg2.Norg2Client
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

object AppModule {
    val appModule =
        module {

            singleOf(::UnleashService) { bind<ToggleableFeatureService>() }

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

            singleOf(::PdlService)

            single {
                CachedMsGraphClient(
                    MsGraphHttpClient(EnvironmentUtils.getRequiredProperty("MS_GRAPH_URL")),
                )
            }

            single {
                val oboflowTokenProvider: OnBehalfOfTokenClient = get()
                AzureADServiceImpl(
                    graphUrl = Url(EnvironmentUtils.getRequiredProperty("MS_GRAPH_URL")),
                    tokenClient = oboflowTokenProvider.bindTo(EnvironmentUtils.getRequiredProperty("MS_GRAPH_SCOPE")),
                )
            }

            single {
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

            single {
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

                Norg2Client(EnvironmentUtils.getRequiredProperty("NORG2_API_URL"), client)
            }
        }
}
