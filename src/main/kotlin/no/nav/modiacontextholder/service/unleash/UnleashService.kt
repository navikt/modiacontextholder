package no.nav.modiacontextholder.service.unleash

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.UnleashContextProvider
import io.getunleash.util.UnleashConfig
import no.nav.common.utils.EnvironmentUtils

class UnleashService(
    unleashContextProvider: UnleashContextProvider,
) : ToggleableFeatureService {
    private val api: String = EnvironmentUtils.getRequiredProperty("UNLEASH_SERVER_API_URL") + "/api"
    private val apiToken: String = EnvironmentUtils.getRequiredProperty("UNLEASH_SERVER_API_TOKEN")

    private val unleash: Unleash

    init {
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

        unleash = DefaultUnleash(unleashConfig)
    }

    override fun isEnabled(feature: ToggleableFeature): Boolean = isEnabled(feature.featureName)

    override fun isEnabled(feature: String): Boolean = unleash.isEnabled(feature)
}
