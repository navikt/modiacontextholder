package no.nav.sbl.redis.model

enum class RedisEventType {
    AKTIV_BRUKER,
    AKTIV_ENHET,
    ;

    fun toDomainEventType(): String =
        when (this) {
            AKTIV_BRUKER -> "NY_AKTIV_BRUKER"
            AKTIV_ENHET -> "NY_AKTIV_ENHET"
        }

    companion object {
        fun from(eventType: String): RedisEventType =
            when (eventType) {
                "NY_AKTIV_BRUKER" -> AKTIV_BRUKER
                "NY_AKTIV_ENHET" -> AKTIV_ENHET
                else -> throw IllegalArgumentException("Ukjent eventType: $eventType")
            }
    }
}