package no.nav.sbl.consumers.modiacontextholder

import no.nav.sbl.rest.domain.RSAktivBruker
import no.nav.sbl.rest.domain.RSAktivEnhet
import no.nav.sbl.rest.domain.RSContext
import no.nav.sbl.rest.domain.RSNyContext

interface ModiaContextHolderClient {
    fun hentVeiledersContext(veilederIdent: String): RSContext
    fun oppdaterVeiledersContext(nyContext: RSNyContext, veilederIdent: String)
    fun hentAktivBruker(veilederIdent: String): RSContext
    fun hentAktivBrukerV2(veilederIdent: String): RSAktivBruker
    fun hentAktivEnhet(veilederIdent: String): RSContext
    fun hentAktivEnhetV2(veilederIdent: String): RSAktivEnhet
    fun nullstillContext(veilederident: String)
    fun nullstillAktivBruker(veilederIdent: String)
}