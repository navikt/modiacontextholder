package no.nav.sbl.service.unleash


interface ToggleableFeatureService {
    fun isEnabled(feature: ToggleableFeature): Boolean
    fun isEnabled(feature: String): Boolean
}
