package no.nav.modiacontextholder.service

import no.nav.common.client.nom.NomClient
import no.nav.common.client.nom.VeilederNavn
import no.nav.common.types.identer.NavIdent
import no.nav.modiacontextholder.rest.model.DecoratorDomain
import no.nav.modiacontextholder.utils.CacheFactory

open class VeilederService(
    private val nomClient: NomClient,
) {
    private val cache = CacheFactory.createCache<String, DecoratorDomain.Saksbehandler>()

    open fun hentVeilederNavn(ident: String): DecoratorDomain.Saksbehandler =
        cache.get(ident) {
            val veilederNavn: VeilederNavn = nomClient.finnNavn(NavIdent(ident))

            DecoratorDomain.Saksbehandler(
                ident,
                veilederNavn.fornavn,
                veilederNavn.etternavn,
            )
        }
}
