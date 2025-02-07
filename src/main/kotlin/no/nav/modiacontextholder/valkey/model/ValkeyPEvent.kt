package no.nav.modiacontextholder.valkey.model

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import no.nav.modiacontextholder.domain.VeilederContext

@Serializable
data class ValkeyPEvent(
    val veilederIdent: String,
    val contextType: ValkeyVeilederContextType,
    val verdi: String,
    val created: LocalDateTime,
) {
    val key: ValkeyPEventKey = ValkeyPEventKey(contextType, veilederIdent)

    fun toPEvent(): VeilederContext =
        VeilederContext(
            veilederIdent = veilederIdent,
            contextType = contextType.toDomain(),
            verdi = verdi,
            created = created,
        )

    companion object {
        fun from(veilederContext: VeilederContext): ValkeyPEvent =
            ValkeyPEvent(
                veilederIdent = veilederContext.veilederIdent,
                contextType = ValkeyVeilederContextType.from(veilederContext.contextType),
                verdi = veilederContext.verdi,
                created = veilederContext.created ?: Clock.System.now().toLocalDateTime(TimeZone.UTC),
            )
    }
}
