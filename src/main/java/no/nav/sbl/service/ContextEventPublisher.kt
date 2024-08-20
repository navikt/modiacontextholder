package no.nav.sbl.service

interface ContextEventPublisher {
    fun publishMessage(
        ident: String,
        eventType: String,
    )
}
