package no.nav.sbl.config

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.UnleashContextProvider
import io.getunleash.util.UnleashConfig
import no.nav.common.utils.EnvironmentUtils
import no.nav.sbl.service.AuthContextService
import no.nav.sbl.service.unleash.ToggleableFeatureService
import no.nav.sbl.service.unleash.UnleashContextProviderImpl
import no.nav.sbl.service.unleash.UnleashService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class FeatureToggleConfig {
    private val api: String = EnvironmentUtils.getRequiredProperty("UNLEASH_SERVER_API_URL") + "/api"
    private val apiToken: String = EnvironmentUtils.getRequiredProperty("UNLEASH_SERVER_API_TOKEN")

    @Bean
    open fun unleashService(unleashContextProvider: UnleashContextProvider): ToggleableFeatureService {
        val unleashConfig =
            UnleashConfig
                .builder()
                .apply {
                    appName("modiacontextholder")
                    environment(System.getProperty("UNLEASH_ENVIRONMENT"))
                    instanceId(System.getProperty("APP_ENVIRONMENT_NAME", "local"))
                    unleashAPI(api)
                    apiKey(apiToken)
                    unleashContextProvider(unleashContextProvider)
                    synchronousFetchOnInitialisation(true)
                }.build()

        val defaultUnleash: Unleash = DefaultUnleash(unleashConfig)

        return UnleashService(defaultUnleash)
    }

    @Bean
    @Autowired
    open fun unleashContextProvider(authContextService: AuthContextService): UnleashContextProvider =
        UnleashContextProviderImpl(authContextService)
}
