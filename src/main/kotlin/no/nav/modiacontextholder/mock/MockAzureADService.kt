package no.nav.modiacontextholder.mock

import no.nav.common.types.identer.AzureObjectId
import no.nav.common.types.identer.NavIdent
import no.nav.modiacontextholder.service.AnsattRolle
import no.nav.modiacontextholder.service.AzureADService

class MockAzureADService : AzureADService {
    companion object {
        val GROUP_ID = AzureObjectId("d2987104-63b2-4110-83ac-20ff6afe24a2")
        val GROUP_NAME = "0000-GA-GOSYS_REGIONAL"
        val VEILEDER_NAV_IDENT = NavIdent("FK12345")
    }

    override fun fetchRoller(
        userToken: String,
        veilederIdent: NavIdent,
    ): List<AnsattRolle> =
        listOf(
            AnsattRolle(GROUP_NAME, GROUP_ID),
        )
}
