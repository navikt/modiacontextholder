package no.nav.modiacontextholder.redis.model

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import no.nav.modiacontextholder.domain.VeilederContext

@Serializable
data class RedisPEvent(
    val veilederIdent: String,
    val contextType: RedisVeilederContextType,
    val verdi: String,
    val created: LocalDateTime,
) {
    val key: RedisPEventKey = RedisPEventKey(contextType, veilederIdent)

    fun toPEvent(): VeilederContext =
        VeilederContext(
            veilederIdent = veilederIdent,
            contextType = contextType.toDomain(),
            verdi = verdi,
            created = created,
        )

    companion object {
        fun from(veilederContext: VeilederContext): RedisPEvent =
            RedisPEvent(
                veilederIdent = veilederContext.veilederIdent,
                contextType = RedisVeilederContextType.from(veilederContext.contextType),
                verdi = veilederContext.verdi,
                created = veilederContext.created ?: Clock.System.now().toLocalDateTime(TimeZone.UTC),
            )
    }
}
