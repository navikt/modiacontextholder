package no.nav.modiacontextholder.rest.model

import kotlinx.serialization.Serializable

class DecoratorDomain {
    @Serializable
    data class DecoratorConfig(
        private val saksbehandler: Saksbehandler,
        val enheter: List<Enhet>,
    ) {
        val ident: String = saksbehandler.ident
        val navn: String = saksbehandler.navn
        val fornavn: String = saksbehandler.fornavn
        val etternavn: String = saksbehandler.etternavn
    }

    @Serializable
    data class Enhet(
        val enhetId: String,
        val navn: String,
        val gruppeId: String? = null,
    )

    @Serializable
    data class Saksbehandler(
        val ident: String,
        val fornavn: String,
        val etternavn: String,
    ) {
        val navn: String = "$fornavn $etternavn"
    }

    @Serializable
    data class FnrAktorId(
        val fnr: String,
        val aktorId: String,
    )
}
