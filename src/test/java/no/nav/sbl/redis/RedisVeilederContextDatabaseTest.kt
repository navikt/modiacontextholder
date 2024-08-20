package no.nav.sbl.redis

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import no.nav.sbl.domain.VeilederContext
import no.nav.sbl.domain.VeilederContextType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class RedisVeilederContextDatabaseTest {
    companion object {
        @Container
        private val redisContainer = TestUtils.RedisContainer()
    }

    @AfterEach
    fun afterEach(): Unit =
        runBlocking {
            authJedisPool.useResource { it.flushAll() }
        }

    private val authJedisPool by lazy {
        AuthJedisPool(
            uriWithAuth =
                RedisUriWithAuth(
                    uri = "redis://${redisContainer.host}:${redisContainer.getMappedPort(6379)}",
                    password = "password",
                    user = "default",
                ),
        )
    }
    private val redisVeilederContextDatabase by lazy {
        RedisVeilederContextDatabase(
            authJedisPool,
            jacksonObjectMapper().registerModule(JavaTimeModule()),
        )
    }

    private val enhetEvent: VeilederContext =
        VeilederContext(
            contextType = VeilederContextType.NY_AKTIV_ENHET,
            verdi = "enhet",
            veilederIdent = "veileder",
        )

    private val brukerEvent =
        VeilederContext(
            contextType = VeilederContextType.NY_AKTIV_BRUKER,
            verdi = "bruker",
            veilederIdent = "veileder",
        )

    @Test
    fun `brukereventer kan lagres og hentes`() {
        redisVeilederContextDatabase.save(brukerEvent)

        val aktivBrukerEvent = redisVeilederContextDatabase.sistAktiveBrukerEvent("veileder")

        assertThat(aktivBrukerEvent?.verdi).isNotNull
        assertThat(aktivBrukerEvent?.verdi).isEqualTo(brukerEvent.verdi)
    }

    @Test
    fun `enheteventer kan lagres og hentes`() {
        redisVeilederContextDatabase.save(enhetEvent)

        val aktivEnhetEvent = redisVeilederContextDatabase.sistAktiveEnhetEvent("veileder")

        assertThat(aktivEnhetEvent?.verdi).isNotNull
        assertThat(aktivEnhetEvent?.verdi).isEqualTo(enhetEvent.verdi)
    }

    @Test
    fun `sletter alle enheteventer for en veileder`() {
        redisVeilederContextDatabase.save(enhetEvent)
        redisVeilederContextDatabase.save(brukerEvent)

        redisVeilederContextDatabase.slettAlleAvEventTypeForVeileder(VeilederContextType.NY_AKTIV_ENHET, "veileder")

        val aktivEnhetEvent = redisVeilederContextDatabase.sistAktiveEnhetEvent("veileder")
        val aktivBrukerEvent = redisVeilederContextDatabase.sistAktiveBrukerEvent("veileder")

        assertThat(aktivEnhetEvent).isNull()
        assertThat(aktivBrukerEvent?.verdi).isNotNull
        assertThat(aktivBrukerEvent?.verdi).isEqualTo(brukerEvent.verdi)
    }

    @Test
    fun `sletter alle brukereventer for en veileder`() {
        redisVeilederContextDatabase.save(brukerEvent)
        redisVeilederContextDatabase.save(enhetEvent)

        redisVeilederContextDatabase.slettAlleAvEventTypeForVeileder(VeilederContextType.NY_AKTIV_BRUKER, "veileder")

        val aktivBrukerEvent = redisVeilederContextDatabase.sistAktiveBrukerEvent("veileder")
        val aktivEnhetEvent = redisVeilederContextDatabase.sistAktiveEnhetEvent("veileder")

        assertThat(aktivBrukerEvent).isNull()
        assertThat(aktivEnhetEvent?.verdi).isNotNull
        assertThat(aktivEnhetEvent?.verdi).isEqualTo(enhetEvent.verdi)
    }

    @Test
    fun `alle eventer for en veileder kan slettes`() {
        redisVeilederContextDatabase.save(brukerEvent)
        redisVeilederContextDatabase.save(enhetEvent)

        redisVeilederContextDatabase.slettAlleEventer("veileder")

        val aktivBrukerEvent = redisVeilederContextDatabase.sistAktiveBrukerEvent("veileder")
        val aktivEnhetEvent = redisVeilederContextDatabase.sistAktiveEnhetEvent("veileder")

        assertThat(aktivBrukerEvent).isNull()
        assertThat(aktivEnhetEvent).isNull()
    }
}
