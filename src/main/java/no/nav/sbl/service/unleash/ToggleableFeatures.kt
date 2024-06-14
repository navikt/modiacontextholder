package no.nav.sbl.service.unleash

enum class ToggleableFeatures : ToggleableFeature {
    HENT_CONTEXT_FRA_GCP {
        override val featureName: String = "modiapersonoversikt.hent-context-fra-gcp"
    },
    SEND_CONTEXT_TIL_GCP {
        override val featureName: String = "modiapersonoversikt.send-context-til-gcp"
    }
}
