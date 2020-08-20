package no.nav.sbl.dao

import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.sbl.transactional
import javax.sql.DataSource

private const val table = "modiacontextholder"


class ContextDAOImpl(private val dataSource: DataSource) : ContextDAO {
    override suspend fun delete(data: String) {
        return transactional(dataSource) { tx -> slettAlleEventer(tx,data) }
    }

    override suspend fun getAktivEnhet(data: String): EventDTO? {
        return transactional(dataSource){ tx -> sisteAktiveEnhetEvent(tx, data) }
    }
    override suspend fun getAktivBruker(data: String): EventDTO? {
        return transactional(dataSource){ tx -> sisteAktiveBrukerEvent(tx, data)}
    }

}


