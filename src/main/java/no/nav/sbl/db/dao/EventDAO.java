package no.nav.sbl.db.dao;

import no.nav.common.utils.EnvironmentUtils;
import no.nav.sbl.db.domain.PEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static no.nav.sbl.db.dao.DbUtil.*;

@Transactional
public class EventDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public long save(PEvent pEvent) {
        if ((List.of("dev-gcp", "prod-gcp").contains(EnvironmentUtils.getRequiredProperty("NAIS_CLUSTER_NAME")))) {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("EVENT")
                    .usingGeneratedKeyColumns("event_id");
            return jdbcInsert.executeAndReturnKey(
                    Map.of(
                        "veileder_ident", pEvent.veilederIdent,
                        "event_type", pEvent.eventType,
                        "verdi", pEvent.verdi,
                        "created", convert(now())
                    )
            ).longValue();
        } else {
            Long nesteSekvensverdi = nesteSekvensverdi("EVENT_ID_SEQ", jdbcTemplate);
            Map namedParameters = new HashMap();
            namedParameters.put("event_id", nesteSekvensverdi);
            namedParameters.put("veileder_ident", pEvent.veilederIdent);
            namedParameters.put("event_type", pEvent.eventType);
            namedParameters.put("verdi", pEvent.verdi);
            namedParameters.put("created", convert(now()));
            namedParameterJdbcTemplate.update("insert into event " +
                    "(event_id, veileder_ident, event_type, verdi, created)" +
                    "VALUES (:event_id, :veileder_ident, :event_type, :verdi, :created)", namedParameters);
            return nesteSekvensverdi;
        }
    }

    private class EventMapper implements RowMapper<PEvent> {
        public PEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new PEvent()
                    .id(rs.getLong("event_id"))
                    .veilederIdent(rs.getString("veileder_ident"))
                    .eventType(rs.getString("event_type"))
                    .verdi(rs.getString("verdi"))
                    .created(convert(rs.getTimestamp("created")));
        }
    }

    public Optional<PEvent> sistAktiveBrukerEvent(String veilederIdent) {
        return queryOptional(jdbcTemplate,
                "select * from (select * from event where veileder_ident = ? and event_type = 'NY_AKTIV_BRUKER' order by created desc) where ROWNUM = 1",
                new EventMapper(),
                veilederIdent);
    }

    public Optional<PEvent> sistAktiveEnhetEvent(String veilederIdent) {
        return queryOptional(jdbcTemplate,
                "select * from (select * from event where veileder_ident = ? and event_type = 'NY_AKTIV_ENHET' order by created desc) where ROWNUM = 1",
                new EventMapper(),
                veilederIdent);
    }

    public List<PEvent> finnAlleEventerEtterId(long id) {
        return jdbcTemplate.query("select * from event where event_id > ?", new EventMapper(), id);
    }

    public void slettAlleAvEventType(String eventType) {
        jdbcTemplate.update("delete from event where event_type = ?", eventType);
    }

    public void slettAlleAvEventTypeForVeileder(String eventType, String veilederIdent) {
        jdbcTemplate.update("delete from event where event_type = ? and veileder_ident = ?", eventType, veilederIdent);
    }

    public void slettAlleEventerUtenomNyeste(List<PEvent> eventer) {
        eventer.stream().sorted((o1, o2) -> o2.id < o1.id ? 1 : -1)
                .limit(eventer.size() - 1)
                .forEach(pEvent -> deleteEvent(pEvent.id));
    }

    public void deleteEvent(long id) {
        jdbcTemplate.update("delete from event where event_id = ?", id);
    }

    public List<PEvent> hentVeiledersEventerAvType(String eventType, String veilederIdent) {
        return jdbcTemplate.query("select * from event where veileder_ident = ? and event_type = ?", new EventMapper(), veilederIdent, eventType);
    }

    public List<String> hentUnikeVeilederIdenter() {
        return jdbcTemplate.query("select distinct veileder_ident from event", (rs, i) -> rs.getString("veileder_ident"));
    }

    public void slettAllEventer(String veilederIdent) {
        jdbcTemplate.update("delete from event where veileder_ident = ?", veilederIdent);
    }
}
