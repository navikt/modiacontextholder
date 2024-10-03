package no.nav.modiacontextholder.utils

import io.ktor.server.application.*

class RequestContextProvider(
    private val call: ApplicationCall,
) {
    fun getApplicationCall() = call
}
