package no.nav.sbl;

import no.nav.sbl.db.dao.EventDAO;
import no.nav.sbl.db.domain.PEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class EventDAOTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    @InjectMocks
    private EventDAO eventDAO;

    @Test
    public void slettAlleEventerUtenomNyesteSletterFaktiskNyeste() {
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> args = ArgumentCaptor.forClass(Long.class);

        eventDAO.slettAlleEventerUtenomNyeste(asList(
                new PEvent()
                        .id(1L)
                        .verdi("1"),
                new PEvent()
                        .id(4L)
                        .verdi("4"),
                new PEvent()
                        .id(2L)
                        .verdi("2"),
                new PEvent()
                        .id(3L)
                        .verdi("3")
        ));

        verify(jdbcTemplate, times(3)).update(sql.capture(), args.capture());
        assertThat(sql.getAllValues()).contains("delete from event where event_id = ?");
        assertThat(args.getAllValues()).contains(1L);
        assertThat(args.getAllValues()).contains(2L);
        assertThat(args.getAllValues()).contains(3L);
    }
}
