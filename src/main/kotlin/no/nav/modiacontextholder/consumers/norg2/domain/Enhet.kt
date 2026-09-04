package no.nav.modiacontextholder.consumers.norg2.domain

import kotlinx.serialization.Serializable

@Serializable
data class Enhet(
    val enhetNr: String,
    val navn: String,
    val status: String,
    val type: String? = null,
    val gruppeId: String? = null,
    val oppgavebehandler: Boolean? = null,
)
