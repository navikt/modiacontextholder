package no.nav.sbl.service.unleash

enum class Feature(val propertyKey: String) {
    SAMPLE_FEATURE("feature.samplerfeature"),
    HENT_CONTEXT_FRA_GCP("modiapersonoversikt.hent-context-fra-gcp"),
    SEND_CONTEXT_TIL_GCP("modiapersonoversikt.send-context-til-gcp"),
}
