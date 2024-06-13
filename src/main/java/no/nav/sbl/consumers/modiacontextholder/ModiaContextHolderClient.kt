package no.nav.sbl.consumers.modiacontextholder

import no.nav.sbl.rest.domain.RSAktivBruker
import no.nav.sbl.rest.domain.RSAktivEnhet
import no.nav.sbl.rest.domain.RSContext
import no.nav.sbl.rest.domain.RSNyContext

interface ModiaContextHolderClient {
    fun hentVeiledersContext(veilederIdent: String): Result<RSContext>
    fun oppdaterVeiledersContext(nyContext: RSNyContext, veilederIdent: String): Result<Unit>
    fun hentAktivBruker(veilederIdent: String): Result<RSContext>
    fun hentAktivBrukerV2(veilederIdent: String): Result<RSAktivBruker>
    fun hentAktivEnhet(veilederIdent: String): Result<RSContext>
    fun hentAktivEnhetV2(veilederIdent: String): Result<RSAktivEnhet>
    fun nullstillContext(veilederident: String): Result<Unit>
    fun nullstillAktivBruker(veilederIdent: String): Result<Unit>
}