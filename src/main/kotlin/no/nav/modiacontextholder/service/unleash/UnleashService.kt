package no.nav.modiacontextholder.service.unleash

import io.getunleash.Unleash

class UnleashService(
    private val unleash: Unleash,
) : ToggleableFeatureService {
    override fun isEnabled(feature: ToggleableFeature): Boolean = isEnabled(feature.featureName)

    override fun isEnabled(feature: String): Boolean = unleash.isEnabled(feature)
}
