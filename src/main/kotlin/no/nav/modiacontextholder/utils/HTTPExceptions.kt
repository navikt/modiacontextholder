package no.nav.modiacontextholder.utils

import io.ktor.http.HttpStatusCode

class AuthorizationException(
    message: String,
) : RuntimeException(message)

class HTTPException(
    private val statusCode: HttpStatusCode,
    message: String,
) : Throwable(message) {
    fun statusCode() = statusCode
}
