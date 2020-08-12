package no.nav.sbl.consumers.norg2

import no.nav.common.utils.IdUtils
import no.nav.log.MDCConstants
import no.nav.sbl.consumers.axsys.domain.HttpRequestConstants
import no.nav.sbl.consumers.norg2.domain.Enhet
import no.nav.sbl.rest.RestUtils
import org.apache.http.HttpHeaders
import org.slf4j.MDC
import java.lang.RuntimeException
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType

private val Norg2EnheterResponse = object : GenericType<List<Enhet>>() {}
class Norg2Client(url: String, private val systemUser: String) {
    private val client: WebTarget = RestUtils.createClient().target(url)
    private val pingClient: WebTarget = RestUtils.createClient().target(url.replace("/api", "/internal/isAlive"))

    fun hentAlleEnheter(): List<Enhet> {
        val callId = MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()
        val consumerId = MDC.get(MDCConstants.MDC_CONSUMER_ID) ?: systemUser

        return client
                .path("/v1/enhet")
                .queryParam("enhetStatusListe", "AKTIV", "UNDER_ETABLERING", "UNDER_AVVIKLING")
                .request()
                .header(HttpRequestConstants.HEADER_NAV_CALL_ID, callId)
                .header(HttpRequestConstants.HEADER_NAV_CONSUMER_ID, consumerId)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .get(Norg2EnheterResponse)
    }

    fun ping() {
        val status = pingClient.request().get().status
        if (status != 200) {
            throw RuntimeException("Norg2 /isAlive status: $status")
        }
    }
}
