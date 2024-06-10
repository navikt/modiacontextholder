package no.nav.sbl.service

import no.nav.common.client.nom.NomClient
import no.nav.common.client.nom.VeilederNavn
import no.nav.common.types.identer.NavIdent
import no.nav.sbl.rest.domain.DecoratorDomain
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable

open class VeilederService(private val nomClient: NomClient) {

    @Cacheable("veilederCache")
    open fun hentVeilederNavn(ident: String): DecoratorDomain.Saksbehandler {
        val veilederNavn: VeilederNavn = nomClient.finnNavn(NavIdent(ident))

        return DecoratorDomain.Saksbehandler(
            ident,
            veilederNavn.fornavn,
            veilederNavn.etternavn
        )
    }
}