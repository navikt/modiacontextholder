package no.nav.sbl.db

import no.nav.sbl.db.dao.EventDAO
import no.nav.sbl.db.domain.PEvent
import no.nav.sbl.redis.RedisVeilederContextDatabase
import no.nav.sbl.service.unleash.ToggleableFeatureService
import no.nav.sbl.service.unleash.ToggleableFeatures

class SwitchingVeilederContextDatabase(
    private val toggleableFeatureService: ToggleableFeatureService,
    private val eventDao: EventDAO,
    private val redisVeilederContextDatabase: RedisVeilederContextDatabase,
) : VeilederContextDatabase {
    private val delegate: VeilederContextDatabase
        get() =
            if (toggleableFeatureService.isEnabled(ToggleableFeatures.USE_REDIS_FOR_VEILEDER_CONTEXT)) {
                redisVeilederContextDatabase
            } else {
                eventDao
            }

    override fun save(pEvent: PEvent) = delegate.save(pEvent)

    override fun sistAktiveBrukerEvent(veilederIdent: String): PEvent? = delegate.sistAktiveBrukerEvent(veilederIdent)

    override fun sistAktiveEnhetEvent(veilederIdent: String): PEvent? = delegate.sistAktiveEnhetEvent(veilederIdent)

    override fun slettAlleEventer(veilederIdent: String) = delegate.slettAlleEventer(veilederIdent)

    override fun slettAlleAvEventTypeForVeileder(
        eventType: String,
        veilederIdent: String,
    ) = delegate.slettAlleAvEventTypeForVeileder(eventType, veilederIdent)
}
