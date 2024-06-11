package no.nav.sbl.service.unleash


interface UnleashService {
    fun isEnabled(feature: Feature): Boolean
    fun isEnabled(feature: String): Boolean
}
