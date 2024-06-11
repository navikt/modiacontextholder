package no.nav.sbl.service.unleash

import io.getunleash.Unleash

class UnleashServiceImpl(
    private val defaultUnleash: Unleash
) : UnleashService {
    override fun isEnabled(feature: Feature): Boolean {
        return defaultUnleash.isEnabled(feature.propertyKey)
    }

    override fun isEnabled(feature: String): Boolean {
        return defaultUnleash.isEnabled(feature)
    }
}
