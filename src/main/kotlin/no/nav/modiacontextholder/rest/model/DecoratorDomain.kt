package no.nav.modiacontextholder.rest.model

class DecoratorDomain {
    data class DecoratorConfig(
        private val saksbehandler: Saksbehandler,
        val enheter: List<Enhet>,
    ) {
        val ident: String = saksbehandler.ident
        val navn: String = saksbehandler.navn
        val fornavn: String = saksbehandler.fornavn
        val etternavn: String = saksbehandler.etternavn
    }

    data class Enhet(
        val enhetId: String,
        val navn: String,
    )

    data class Saksbehandler(
        val ident: String,
        val fornavn: String,
        val etternavn: String,
    ) {
        val navn: String = "$fornavn $etternavn"
    }

    data class FnrAktorId(
        val fnr: String,
        val aktorId: String,
    )
}
