package no.nav.modiacontextholder.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import kotlinx.coroutines.runBlocking
import no.nav.modiacontextholder.domain.VeilederContext
import no.nav.modiacontextholder.domain.VeilederContextType
import org.assertj.core.api.Assertions.assertThat
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.AfterTest
import kotlin.test.Test

@Testcontainers
class RedisVeilederContextDatabaseTest {
    companion object {
        @Container
        private val redisContainer = TestUtils.RedisContainer()
    }

    @AfterTest
    fun afterEach(): Unit =
        runBlocking {
            redis.flushall()
        }

    private val redisConnection by lazy {
        RedisClient
            .create(
                RedisURI
                    .builder()
                    .withHost(redisContainer.host)
                    .withPort(redisContainer.getMappedPort(6379))
                    .withAuthentication("default", "password")
                    .build(),
            ).connect()
    }
    private val redis = redisConnection.sync()
    private val redisVeilederContextDatabase by lazy {
        RedisVeilederContextDatabase(
            redisConnection,
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
