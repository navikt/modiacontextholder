package no.nav.sbl.dao

import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.action.ResultQueryActionBuilder
import no.nav.sbl.execute
import no.nav.sbl.transactional
import javax.sql.DataSource

class EventDAOImpl (private val dataSource: DataSource) : EventDAO {
    override suspend fun get(data: String) : List<EventDTO> {
        return transactional(dataSource) {tx -> finnAlleEventerEtterId(tx, data)}
    }

}
private fun finnAlleEventerEtterId(tx: TransactionalSession, id: String) : List<EventDTO> {
    return getQuery(id)
            .asList
            .execute(tx)
}

fun slettAlleEventer(tx: TransactionalSession, veilederIdent: String) : Int {
    return queryOf("delete from event where veileder_ident = ?", veilederIdent)
            .asUpdate
            .execute(tx)
}
fun sisteAktiveEnhetEvent(tx: TransactionalSession, veilederIdent: String) : EventDTO? {
    return queryOf("select * from (select * from event where veileder_ident = ? and event_type = 'NY_AKTIV_ENHET' order by created desc) where ROWNUM = 1", veilederIdent, "NY_AKTIV_ENHET")
            .map { row ->
                EventDTO(
                        row.string("id"),
                        row.string("veilederIdent"),
                        row.string("eventType"),
                        row.localDateTime("created"),
                        row.string("verdi")
                )
            }
            .asSingle
            .execute(tx)
}

fun sisteAktiveBrukerEvent(tx: TransactionalSession, veilederIdent: String) : EventDTO? {
    return queryOf("select * from (select * from event where veileder_ident = ? and event_type = 'NY_AKTIV_BRUKER' order by created desc) where ROWNUM = 1", veilederIdent, "NY_AKTIV_ENHET")
            .map { row ->
                EventDTO(
                        row.string("id"),
                        row.string("veilederIdent"),
                        row.string("eventType"),
                        row.localDateTime("created"),
                        row.string("verdi")
                )
            }
            .asSingle
            .execute(tx)
}

private fun getQuery(id: String): ResultQueryActionBuilder<EventDTO> {
    return queryOf("SELECT * FROM event WHERE event_id > ?", id)
            .map { row ->
                EventDTO(
                        row.string("id"),
                        row.string("veilederIdent"),
                        row.string("eventType"),
                        row.localDateTime("created"),
                        row.string("verdi")
                )
            }
}