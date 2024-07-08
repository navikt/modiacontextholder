package no.nav.sbl.db

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.sbl.db.dao.EventDAO
import no.nav.sbl.db.domain.PEvent
import no.nav.sbl.redis.RedisVeilederContextDatabase
import no.nav.sbl.service.unleash.ToggleableFeatureService
import no.nav.sbl.service.unleash.ToggleableFeatures
import org.junit.jupiter.api.Test

class SwitchingVeilederContextDatabaseTest {
    private val toggleableFeatureService = mockk<ToggleableFeatureService>()
    private val eventDao = mockk<EventDAO>(relaxed = true)
    private val redisVeilederContextDatabase = mockk<RedisVeilederContextDatabase>(relaxed = true)
    private val switchingVeilederContextDatabase =
        SwitchingVeilederContextDatabase(
            toggleableFeatureService,
            eventDao,
            redisVeilederContextDatabase,
        )

    @Test
    fun `save skal bruke redis veileder context database hvis feature er på`() {
        every { toggleableFeatureService.isEnabled(ToggleableFeatures.USE_REDIS_FOR_VEILEDER_CONTEXT) } returns true

        switchingVeilederContextDatabase.save(PEvent())

        verify { redisVeilederContextDatabase.save(any()) }
        verify(exactly = 0) { eventDao.save(any()) }
    }

    @Test
    fun `save skal bruke event dao hvis feature er av`() {
        every { toggleableFeatureService.isEnabled(ToggleableFeatures.USE_REDIS_FOR_VEILEDER_CONTEXT) } returns false

        switchingVeilederContextDatabase.save(PEvent())

        verify(exactly = 0) { redisVeilederContextDatabase.save(any()) }
        verify { eventDao.save(any()) }
    }

    @Test
    fun `sistAktiveBrukerEvent skal bruke redis veileder context database hvis feature er på`() {
        every { toggleableFeatureService.isEnabled(ToggleableFeatures.USE_REDIS_FOR_VEILEDER_CONTEXT) } returns true

        switchingVeilederContextDatabase.sistAktiveBrukerEvent("veileder")

        verify { redisVeilederContextDatabase.sistAktiveBrukerEvent(any()) }
        verify(exactly = 0) { eventDao.sistAktiveBrukerEvent(any()) }
    }

    @Test
    fun `sistAktiveBrukerEvent skal bruke event dao hvis feature er av`() {
        every { toggleableFeatureService.isEnabled(ToggleableFeatures.USE_REDIS_FOR_VEILEDER_CONTEXT) } returns false

        switchingVeilederContextDatabase.sistAktiveBrukerEvent("veileder")

        verify(exactly = 0) { redisVeilederContextDatabase.sistAktiveBrukerEvent(any()) }
        verify { eventDao.sistAktiveBrukerEvent(any()) }
    }

    @Test
    fun `sistAktiveEnhetEvent skal bruke redis veileder context database hvis feature er på`() {
        every { toggleableFeatureService.isEnabled(ToggleableFeatures.USE_REDIS_FOR_VEILEDER_CONTEXT) } returns true

        switchingVeilederContextDatabase.sistAktiveEnhetEvent("veileder")

        verify { redisVeilederContextDatabase.sistAktiveEnhetEvent(any()) }
        verify(exactly = 0) { eventDao.sistAktiveEnhetEvent(any()) }
    }

    @Test
    fun `sistAktiveEnhetEvent skal bruke event dao hvis feature er av`() {
        every { toggleableFeatureService.isEnabled(ToggleableFeatures.USE_REDIS_FOR_VEILEDER_CONTEXT) } returns false

        switchingVeilederContextDatabase.sistAktiveEnhetEvent("veileder")

        verify(exactly = 0) { redisVeilederContextDatabase.sistAktiveEnhetEvent(any()) }
        verify { eventDao.sistAktiveEnhetEvent(any()) }
    }

    @Test
    fun `slettAlleEventer skal bruke redis veileder context database hvis feature er på`() {
        every { toggleableFeatureService.isEnabled(ToggleableFeatures.USE_REDIS_FOR_VEILEDER_CONTEXT) } returns true

        switchingVeilederContextDatabase.slettAlleEventer("veileder")

        verify { redisVeilederContextDatabase.slettAlleEventer(any()) }
        verify(exactly = 0) { eventDao.slettAlleEventer(any()) }
    }

    @Test
    fun `slettAlleEventer skal bruke event dao hvis feature er av`() {
        every { toggleableFeatureService.isEnabled(ToggleableFeatures.USE_REDIS_FOR_VEILEDER_CONTEXT) } returns false

        switchingVeilederContextDatabase.slettAlleEventer("veileder")

        verify(exactly = 0) { redisVeilederContextDatabase.slettAlleEventer(any()) }
        verify { eventDao.slettAlleEventer(any()) }
    }

    @Test
    fun `slettAlleAvEventTypeForVeileder skal bruke redis veileder context database hvis feature er på`() {
        every { toggleableFeatureService.isEnabled(ToggleableFeatures.USE_REDIS_FOR_VEILEDER_CONTEXT) } returns true

        switchingVeilederContextDatabase.slettAlleAvEventTypeForVeileder("eventType", "veileder")

        verify { redisVeilederContextDatabase.slettAlleAvEventTypeForVeileder(any(), any()) }
        verify(exactly = 0) { eventDao.slettAlleAvEventTypeForVeileder(any(), any()) }
    }

    @Test
    fun `slettAlleAvEventTypeForVeileder skal bruke event dao hvis feature er av`() {
        every { toggleableFeatureService.isEnabled(ToggleableFeatures.USE_REDIS_FOR_VEILEDER_CONTEXT) } returns false

        switchingVeilederContextDatabase.slettAlleAvEventTypeForVeileder("eventType", "veileder")

        verify(exactly = 0) { redisVeilederContextDatabase.slettAlleAvEventTypeForVeileder(any(), any()) }
        verify { eventDao.slettAlleAvEventTypeForVeileder(any(), any()) }
    }
}
