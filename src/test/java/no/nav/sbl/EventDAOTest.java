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

        PEvent event1 = new PEvent();
        event1.setId(1L);
        event1.setVerdi("1");
        PEvent event2 = new PEvent();
        event2.setId(2L);
        event2.setVerdi("2");
        PEvent event3 = new PEvent();
        event3.setId(3L);
        event3.setVerdi("3");
        PEvent event4 = new PEvent();
        event4.setId(4L);
        event4.setVerdi("4");


        eventDAO.slettAlleEventerUtenomNyeste(asList(
                event1,
                event4,
                event2,
                event3
        ));

        verify(jdbcTemplate, times(3)).update(sql.capture(), args.capture());
        assertThat(sql.getAllValues()).contains("delete from event where event_id = ?");
        assertThat(args.getAllValues()).contains(1L);
        assertThat(args.getAllValues()).contains(2L);
        assertThat(args.getAllValues()).contains(3L);
    }
}
