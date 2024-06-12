package no.nav.sbl.consumers.modiacontextholder

import no.nav.sbl.rest.domain.RSAktivBruker
import no.nav.sbl.rest.domain.RSAktivEnhet
import no.nav.sbl.rest.domain.RSContext
import no.nav.sbl.rest.domain.RSNyContext

class HttpModiaContextHolderClient : ModiaContextHolderClient {
    override fun hentVeiledersContext(veilederIdent: String): RSContext {
        TODO("Not yet implemented")
    }

    override fun oppdaterVeiledersContext(nyContext: RSNyContext, veilederIdent: String) {
        TODO("Not yet implemented")
    }

    override fun hentAktivBruker(veilederIdent: String): RSContext {
        TODO("Not yet implemented")
    }

    override fun hentAktivBrukerV2(veilederIdent: String): RSAktivBruker {
        TODO("Not yet implemented")
    }

    override fun hentAktivEnhet(veilederIdent: String): RSContext {
        TODO("Not yet implemented")
    }

    override fun hentAktivEnhetV2(veilederIdent: String): RSAktivEnhet {
        TODO("Not yet implemented")
    }

    override fun nullstillContext(veilederident: String) {
        TODO("Not yet implemented")
    }

    override fun nullstillAktivBruker(veilederIdent: String) {
        TODO("Not yet implemented")
    }
}