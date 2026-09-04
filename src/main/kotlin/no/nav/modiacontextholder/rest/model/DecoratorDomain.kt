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
        val type: String? = null,
        /**
         * GruppeId er id-en til enheten in Entra ID.
         * Det ble forespurt i august 2025 om å eksponere den i APIet vårt slik at folk
         * kan bruke verdien videre til kall til Entra via MS Graph
         */
        val gruppeId: String? = null,
        /**
         * Om enheten kan behandle oppgaver. `null` betyr at NORG2 ikke oppga verdien,
         * og likestilles med `false`.
         */
        val oppgavebehandler: Boolean? = null,
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
