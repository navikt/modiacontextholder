package no.nav.modiacontextholder.rest

data class FnrRequest(
    val fnr: String,
)

data class CodeRequest(
    val code: String,
)

data class CodeResponse(
    val fnr: String,
    val code: String,
)
