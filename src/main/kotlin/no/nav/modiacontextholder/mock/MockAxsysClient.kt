package no.nav.modiacontextholder.mock

import no.nav.common.client.axsys.AxsysClient
import no.nav.common.client.axsys.AxsysEnhet
import no.nav.common.health.HealthCheckResult
import no.nav.common.types.identer.EnhetId
import no.nav.common.types.identer.NavIdent

class MockAxsysClient : AxsysClient {
    override fun hentAnsatte(enhetId: EnhetId?): MutableList<NavIdent> = mutableListOf(NavIdent("Z999999"))

    override fun hentTilganger(ident: NavIdent?): MutableList<AxsysEnhet> =
        mutableListOf(
            AxsysEnhet().apply {
                enhetId = EnhetId("9999")
                navn = "Test enhet"
            },
            AxsysEnhet().apply {
                enhetId = EnhetId("0001")
                navn = "Test økonomienhet"
            },
            AxsysEnhet().apply {
                enhetId = EnhetId("0002")
                navn = "Test lokalenhet 4"
            },
        )

    override fun checkHealth(): HealthCheckResult = HealthCheckResult.healthy()
}
