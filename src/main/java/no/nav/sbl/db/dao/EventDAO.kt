package no.nav.sbl.db.dao

import no.nav.sbl.config.ApplicationCluster
import no.nav.sbl.db.dao.DbUtil.convert
import no.nav.sbl.db.dao.DbUtil.nesteSekvensverdi
import no.nav.sbl.db.domain.PEvent
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.time.LocalDateTime

@Transactional
open class EventDAO(
    private val jdbcTemplate: JdbcTemplate,
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    private val applicationCluster: ApplicationCluster,
) {
    private val log = LoggerFactory.getLogger(EventDAO::class.java)

    fun save(pEvent: PEvent): Long =
        if (applicationCluster.isGcp()) {
            val jdbcInsert =
                SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("EVENT")
                    .usingGeneratedKeyColumns("event_id")
            jdbcInsert
                .executeAndReturnKey(
                    mapOf(
                        "veileder_ident" to pEvent.veilederIdent,
                        "event_type" to pEvent.eventType,
                        "verdi" to pEvent.verdi,
                        "created" to convert(LocalDateTime.now()),
                    ),
                ).toLong()
        } else {
            val nesteSekvensverdi = nesteSekvensverdi("EVENT_ID_SEQ", jdbcTemplate)
            val namedParameters =
                mapOf(
                    "event_id" to nesteSekvensverdi,
                    "veileder_ident" to pEvent.veilederIdent,
                    "event_type" to pEvent.eventType,
                    "verdi" to pEvent.verdi,
                    "created" to convert(LocalDateTime.now()),
                )
            namedParameterJdbcTemplate.update(
                "insert into event (event_id, veileder_ident, event_type, verdi, created) VALUES (:event_id, :veileder_ident, :event_type, :verdi, :created)",
                namedParameters,
            )
            nesteSekvensverdi
        }

    fun sistAktiveBrukerEvent(veilederIdent: String): PEvent? =
        try {
            if (applicationCluster.isGcp()) {
                jdbcTemplate.queryForObject(
                    "select * from event where veileder_ident = ? and event_type = 'NY_AKTIV_BRUKER' order by created desc limit 1",
                    EventMapper,
                    veilederIdent,
                )!!
            } else {
                jdbcTemplate.queryForObject(
                    "select * from (select * from event where veileder_ident = ? and event_type = 'NY_AKTIV_BRUKER' order by created desc) where ROWNUM = 1",
                    EventMapper,
                    veilederIdent,
                )!!
            }
        } catch (e: DataAccessException) {
            log.warn("Feilet ved henting av aktiv bruker", e)
            null
        }

    fun sistAktiveEnhetEvent(veilederIdent: String): PEvent? =
        try {
            if (applicationCluster.isGcp()) {
                jdbcTemplate.queryForObject(
                    "select * from event where veileder_ident = ? and event_type = 'NY_AKTIV_ENHET' order by created desc limit 1",
                    EventMapper,
                    veilederIdent,
                )!!
            } else {
                jdbcTemplate.queryForObject(
                    "select * from (select * from event where veileder_ident = ? and event_type = 'NY_AKTIV_ENHET' order by created desc) where ROWNUM = 1",
                    EventMapper,
                    veilederIdent,
                )!!
            }
        } catch (e: DataAccessException) {
            log.warn("Feilet ved henting av aktiv enhet", e)
            null
        }

    fun finnAlleEventerEtterId(id: Long): List<PEvent> = jdbcTemplate.query("select * from event where event_id > ?", EventMapper, id)

    fun slettAlleAvEventType(eventType: String) {
        jdbcTemplate.update("delete from event where event_type = ?", eventType)
    }

    fun slettAlleAvEventTypeForVeileder(
        eventType: String,
        veilederIdent: String,
    ) {
        jdbcTemplate.update("delete from event where event_type = ? and veileder_ident = ?", eventType, veilederIdent)
    }

    fun slettAlleEventerUtenomNyeste(eventer: List<PEvent>) {
        eventer
            .sortedByDescending { it.id }
            .drop(1)
            .forEach { deleteEvent(it.id!!) }
    }

    fun deleteEvent(id: Long) {
        jdbcTemplate.update("delete from event where event_id = ?", id)
    }

    fun hentVeiledersEventerAvType(
        eventType: String,
        veilederIdent: String,
    ): List<PEvent> =
        jdbcTemplate.query(
            "select * from event where veileder_ident = ? and event_type = ?",
            EventMapper,
            veilederIdent,
            eventType,
        )

    fun hentUnikeVeilederIdenter(): List<String> =
        jdbcTemplate.query("select distinct veileder_ident from event") { rs, _ -> rs.getString("veileder_ident") }

    fun slettAllEventer(veilederIdent: String) {
        jdbcTemplate.update("delete from event where veileder_ident = ?", veilederIdent)
    }

    private object EventMapper : RowMapper<PEvent> {
        override fun mapRow(
            rs: ResultSet,
            rowNum: Int,
        ): PEvent =
            PEvent(
                rs.getLong("event_id"),
                rs.getString("veileder_ident"),
                rs.getString("event_type"),
                convert(rs.getTimestamp("created")),
                rs.getString("verdi"),
            )
    }
}
