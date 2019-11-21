package no.nav.sbl.rest.axsys;

import io.vavr.control.Option;
import no.nav.common.utils.IdUtils;
import no.nav.log.MDCConstants;
import no.nav.sbl.rest.RestUtils;
import no.nav.sbl.service.AxsysTilgangResponse;
import no.nav.tjenester.axsys.api.v1.HttpRequestConstants;
import org.slf4j.MDC;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

public class AxsysClient {
    private final String systemUser;
    private final WebTarget client;
    private final WebTarget pingClient;

    public AxsysClient(String axsysUrl, String systemUser) {
        this.systemUser = systemUser;
        client = RestUtils.createClient().target(axsysUrl);
        pingClient = RestUtils.createClient().target(axsysUrl.replace("/api", "/internal/isAlive"));
    }

    public AxsysTilgangResponse hentTilgang(String ident) {
        String callId = Option.of(MDC.get(MDCConstants.MDC_CALL_ID)).getOrElse(IdUtils::generateId);
        String consumerId = Option.of(MDC.get(MDCConstants.MDC_CONSUMER_ID)).getOrElse(systemUser);

        return client
                .path("/v1/tilgang/" + ident)
                .request()
                .header(HttpRequestConstants.HEADER_NAV_CALL_ID, callId)
                .header(HttpRequestConstants.HEADER_NAV_CONSUMER_ID, consumerId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .get(AxsysTilgangResponse.class);
    }

    public void ping() {
        int status = pingClient.request().get().getStatus();
        if (status != 200) {
            throw new RuntimeException("Axsys /isAlive status: " + status);
        }
    }
}
