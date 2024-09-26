package no.nav.modiacontextholder.mock

import no.nav.common.client.nom.NomClient
import no.nav.common.client.nom.VeilederNavn
import no.nav.common.health.HealthCheckResult
import no.nav.common.types.identer.NavIdent

class MockNomClient : NomClient {
    override fun finnNavn(navIdent: NavIdent): VeilederNavn = lagVeilederNavn(navIdent)

    override fun finnNavn(identer: MutableList<NavIdent>): List<VeilederNavn> = identer.map(::lagVeilederNavn)

    private fun lagVeilederNavn(navIdent: NavIdent): VeilederNavn {
        val ident = navIdent.get()
        val identNr = ident.substring(1)
        return VeilederNavn()
            .setNavIdent(navIdent)
            .setFornavn("F_$identNr")
            .setEtternavn("E_$identNr")
            .setVisningsNavn("F_$identNr E_$identNr")
    }

    override fun checkHealth(): HealthCheckResult = HealthCheckResult.healthy()
}
