package no.nav.modiacontextholder.service

import kotlinx.serialization.serializer
import no.nav.common.client.nom.NomClient
import no.nav.common.client.nom.VeilederNavn
import no.nav.common.types.identer.NavIdent
import no.nav.modiacontextholder.log
import no.nav.modiacontextholder.rest.model.DecoratorDomain
import no.nav.modiacontextholder.utils.CacheFactory

open class VeilederService(
    private val nomClient: NomClient,
) {
    private val cache =
        CacheFactory.createDistributedCache<String, DecoratorDomain.Saksbehandler>(
            name = "veileder",
            serializer = serializer<DecoratorDomain.Saksbehandler>(),
        )

    open suspend fun hentVeilederNavn(ident: String): DecoratorDomain.Saksbehandler =
        cache.get(ident) {
            try {
                val veilederNavn: VeilederNavn = nomClient.finnNavn(NavIdent(ident))

                DecoratorDomain.Saksbehandler(
                    ident,
                    veilederNavn.fornavn,
                    veilederNavn.etternavn,
                )
            } catch (e: Exception) {
                log.error("Feil ved henting av veilederInfo fra NOM", e)
                DecoratorDomain.Saksbehandler(ident, "", "")
            }
        }
}
