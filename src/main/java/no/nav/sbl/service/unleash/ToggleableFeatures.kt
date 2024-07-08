package no.nav.sbl.service.unleash

enum class ToggleableFeatures : ToggleableFeature {
    SYNC_CONTEXT_MED_GCP {
        override val featureName: String = "modiapersonoversikt.sync-context-med-gcp"
    },
    USE_REDIS_FOR_VEILEDER_CONTEXT {
        override val featureName: String = "modiapersonoversikt.use-redis-for-veileder-context"
    },
}
