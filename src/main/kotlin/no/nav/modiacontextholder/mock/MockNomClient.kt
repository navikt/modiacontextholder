package no.nav.modiacontextholder.mock

import no.nav.common.client.nom.NomClient
import no.nav.common.client.nom.VeilederNavn
import no.nav.common.health.HealthCheckResult
import no.nav.common.types.identer.NavIdent

class MockNomClient : NomClient {
    override fun finnNavn(identer: MutableList<NavIdent>?): MutableList<VeilederNavn> {
        val navn =
            (identer ?: mutableListOf(NavIdent("Z999999"))).mapIndexed { index, ident ->
                val navn = VeilederNavn()
                navn.fornavn = "Navn $index"
                navn.etternavn = "Navnesen $index"
                navn
            }
        return navn.toMutableList()
    }

    override fun finnNavn(ident: NavIdent?): VeilederNavn {
        val navn = VeilederNavn()
        navn.fornavn = "Navn"
        navn.etternavn = "Navnesen"

        return navn
    }

    override fun checkHealth(): HealthCheckResult = HealthCheckResult.healthy()
}
