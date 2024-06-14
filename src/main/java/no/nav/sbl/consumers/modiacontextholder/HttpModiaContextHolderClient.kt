package no.nav.sbl.consumers.modiacontextholder

import no.nav.sbl.rest.domain.RSAktivBruker
import no.nav.sbl.rest.domain.RSAktivEnhet
import no.nav.sbl.rest.domain.RSContext
import no.nav.sbl.rest.domain.RSNyContext

class HttpModiaContextHolderClient : ModiaContextHolderClient {
    override fun hentVeiledersContext(veilederIdent: String): Result<RSContext> {
        TODO("Not yet implemented")
    }

    override fun oppdaterVeiledersContext(nyContext: RSNyContext, veilederIdent: String): Result<Long> {
        TODO("Not yet implemented")
    }

    override fun hentAktivBruker(veilederIdent: String): Result<RSContext> {
        TODO("Not yet implemented")
    }

    override fun hentAktivBrukerV2(veilederIdent: String): Result<RSAktivBruker> {
        TODO("Not yet implemented")
    }

    override fun hentAktivEnhet(veilederIdent: String): Result<RSContext> {
        TODO("Not yet implemented")
    }

    override fun hentAktivEnhetV2(veilederIdent: String): Result<RSAktivEnhet> {
        TODO("Not yet implemented")
    }

    override fun nullstillContext(veilederident: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun nullstillAktivBruker(veilederIdent: String): Result<Unit> {
        TODO("Not yet implemented")
    }
}