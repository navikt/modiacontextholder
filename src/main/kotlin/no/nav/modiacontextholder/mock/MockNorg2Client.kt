package no.nav.modiacontextholder.mock

import no.nav.modiacontextholder.consumers.norg2.Norg2Client
import no.nav.modiacontextholder.consumers.norg2.domain.Enhet

class MockNorg2Client : Norg2Client {
    override fun hentAlleEnheter(): List<Enhet> =
        listOf(
            Enhet("0001", "Test enhet", "test", "LOKAL", "0"),
            Enhet("0002", "Test annen enhet", "test", null, "1"),
            Enhet("9999", "Test enhet", "test", "KO", "2"),
        )
}
