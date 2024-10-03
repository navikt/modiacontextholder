package no.nav.modiacontextholder.rest

import kotlinx.serialization.Serializable

@Serializable
data class FnrRequest(
    val fnr: String,
)

@Serializable
data class CodeRequest(
    val code: String,
)

@Serializable
data class CodeResponse(
    val fnr: String,
    val code: String,
)
