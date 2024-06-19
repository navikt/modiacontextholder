package no.nav.sbl

import io.mockk.*
import no.nav.sbl.db.dao.EventDAO
import no.nav.sbl.db.domain.PEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.jdbc.core.JdbcTemplate

@RunWith(MockitoJUnitRunner::class)
class EventDAOTest {
    private val jdbcTemplate: JdbcTemplate = mockk(relaxed = true)
    private val eventDAO = EventDAO(jdbcTemplate, mockk())

    @Test
    fun `slettAlleEventerUtenomNyeste sletter faktisk nyeste`() {
        val sqlQuery = mutableListOf<String>()
        val args = mutableListOf<Long>()

        val event1 =
            PEvent().apply {
                id = 1L
                verdi = "1"
            }
        val event2 =
            PEvent().apply {
                id = 2L
                verdi = "2"
            }
        val event3 =
            PEvent().apply {
                id = 3L
                verdi = "3"
            }
        val event4 =
            PEvent().apply {
                id = 4L
                verdi = "4"
            }

        eventDAO.slettAlleEventerUtenomNyeste(listOf(event1, event4, event2, event3))

        verify(exactly = 3) { jdbcTemplate.update(capture(sqlQuery), capture(args)) }
        assertThat(sqlQuery).allMatch { it == "delete from event where event_id = ?" }
        assertThat(args).containsExactlyInAnyOrder(1L, 2L, 3L)
    }
}
