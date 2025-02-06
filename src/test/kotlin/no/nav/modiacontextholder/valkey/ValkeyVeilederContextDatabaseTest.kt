package no.nav.modiacontextholder.valkey

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
class ValkeyVeilederContextDatabaseTest {
    companion object {
        @Container
        private val valkeyContainer = TestUtils.ValkeyContainer()
    }

    @AfterTest
    fun afterEach(): Unit =
        runBlocking {
            valkey.flushall()
        }

    private val valkeyConnection by lazy {
        RedisClient
            .create(
                RedisURI
                    .builder()
                    .withHost(valkeyContainer.host)
                    .withPort(valkeyContainer.getMappedPort(6379))
                    .withAuthentication("default", "password")
                    .build(),
            ).connect()
    }
    private val valkey = valkeyConnection.sync()
    private val valkeyVeilederContextDatabase by lazy {
        ValkeyVeilederContextDatabase(
            valkeyConnection,
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
        valkeyVeilederContextDatabase.save(brukerEvent)

        val aktivBrukerEvent = valkeyVeilederContextDatabase.sistAktiveBrukerEvent("veileder")

        assertThat(aktivBrukerEvent?.verdi).isNotNull
        assertThat(aktivBrukerEvent?.verdi).isEqualTo(brukerEvent.verdi)
    }

    @Test
    fun `enheteventer kan lagres og hentes`() {
        valkeyVeilederContextDatabase.save(enhetEvent)

        val aktivEnhetEvent = valkeyVeilederContextDatabase.sistAktiveEnhetEvent("veileder")

        assertThat(aktivEnhetEvent?.verdi).isNotNull
        assertThat(aktivEnhetEvent?.verdi).isEqualTo(enhetEvent.verdi)
    }

    @Test
    fun `sletter alle enheteventer for en veileder`() {
        valkeyVeilederContextDatabase.save(enhetEvent)
        valkeyVeilederContextDatabase.save(brukerEvent)

        valkeyVeilederContextDatabase.slettAlleAvEventTypeForVeileder(VeilederContextType.NY_AKTIV_ENHET, "veileder")

        val aktivEnhetEvent = valkeyVeilederContextDatabase.sistAktiveEnhetEvent("veileder")
        val aktivBrukerEvent = valkeyVeilederContextDatabase.sistAktiveBrukerEvent("veileder")

        assertThat(aktivEnhetEvent).isNull()
        assertThat(aktivBrukerEvent?.verdi).isNotNull
        assertThat(aktivBrukerEvent?.verdi).isEqualTo(brukerEvent.verdi)
    }

    @Test
    fun `sletter alle brukereventer for en veileder`() {
        valkeyVeilederContextDatabase.save(brukerEvent)
        valkeyVeilederContextDatabase.save(enhetEvent)

        valkeyVeilederContextDatabase.slettAlleAvEventTypeForVeileder(VeilederContextType.NY_AKTIV_BRUKER, "veileder")

        val aktivBrukerEvent = valkeyVeilederContextDatabase.sistAktiveBrukerEvent("veileder")
        val aktivEnhetEvent = valkeyVeilederContextDatabase.sistAktiveEnhetEvent("veileder")

        assertThat(aktivBrukerEvent).isNull()
        assertThat(aktivEnhetEvent?.verdi).isNotNull
        assertThat(aktivEnhetEvent?.verdi).isEqualTo(enhetEvent.verdi)
    }

    @Test
    fun `alle eventer for en veileder kan slettes`() {
        valkeyVeilederContextDatabase.save(brukerEvent)
        valkeyVeilederContextDatabase.save(enhetEvent)

        valkeyVeilederContextDatabase.slettAlleEventer("veileder")

        val aktivBrukerEvent = valkeyVeilederContextDatabase.sistAktiveBrukerEvent("veileder")
        val aktivEnhetEvent = valkeyVeilederContextDatabase.sistAktiveEnhetEvent("veileder")

        assertThat(aktivBrukerEvent).isNull()
        assertThat(aktivEnhetEvent).isNull()
    }
}
