package no.nav.modiacontextholder.service.unleash

interface ToggleableFeatureService {
    fun isEnabled(feature: ToggleableFeature): Boolean

    fun isEnabled(feature: String): Boolean
}
