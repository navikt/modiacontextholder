package no.nav.sbl.consumers.modiacontextholder

import no.nav.sbl.rest.model.RSAktivBruker
import no.nav.sbl.rest.model.RSAktivEnhet
import no.nav.sbl.rest.model.RSContext
import no.nav.sbl.rest.model.RSNyContext

interface ModiaContextHolderClient {
    fun hentVeiledersContext(veilederIdent: String): Result<RSContext>
    fun oppdaterVeiledersContext(nyContext: RSNyContext, veilederIdent: String): Result<RSContext>
    fun hentAktivBruker(veilederIdent: String): Result<RSContext>
    fun hentAktivBrukerV2(veilederIdent: String): Result<RSAktivBruker>
    fun hentAktivEnhet(veilederIdent: String): Result<RSContext>
    fun hentAktivEnhetV2(veilederIdent: String): Result<RSAktivEnhet>
    fun nullstillBrukerContext(veilederident: String): Result<Unit>
    fun nullstillAktivBruker(veilederIdent: String): Result<Unit>
}