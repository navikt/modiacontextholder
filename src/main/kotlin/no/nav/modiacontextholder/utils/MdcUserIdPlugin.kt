package no.nav.modiacontextholder.utils

import io.ktor.server.application.createRouteScopedPlugin
import no.nav.common.log.MDCConstants
import org.slf4j.MDC

val mdcUserId =
    createRouteScopedPlugin(name = "mdcUserId") {
        onCall {
            runCatching { it.getIdent() }.onSuccess {
                MDC.put(MDCConstants.MDC_USER_ID, it)
            }
        }
    }
