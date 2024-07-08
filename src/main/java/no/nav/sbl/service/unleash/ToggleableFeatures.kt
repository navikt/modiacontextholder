package no.nav.sbl.service.unleash

enum class ToggleableFeatures : ToggleableFeature {
    SYNC_CONTEXT_MED_GCP {
        override val featureName: String = "modiapersonoversikt.sync-context-med-gcp"
    },
}
