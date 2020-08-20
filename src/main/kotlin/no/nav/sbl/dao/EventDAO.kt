package no.nav.sbl.dao

import java.time.LocalDateTime

data class EventDTO (
        var id: String?,
        var veilederIdent: String,
        var eventType: String,
        var created: LocalDateTime,
        var verdi: String
)
fun EventDTO.toDTO() = EventDTO(id, veilederIdent, eventType, created, verdi)
fun List<EventDTO>.toDTO() = this.map(EventDTO::toDTO)


interface EventDAO {
    suspend fun get (id: String) : List<EventDTO>
    }