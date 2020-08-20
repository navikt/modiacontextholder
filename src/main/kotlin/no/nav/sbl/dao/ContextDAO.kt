package no.nav.sbl.dao

interface ContextDAO {
    suspend fun delete(data: String)
    suspend fun getAktivEnhet(data: String) : EventDTO?
    suspend fun getAktivBruker(data: String): EventDTO?

}