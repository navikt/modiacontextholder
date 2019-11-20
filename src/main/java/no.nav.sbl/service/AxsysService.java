package no.nav.sbl.service;

import io.vavr.control.Option;
import io.vavr.control.Try;
import no.nav.brukerdialog.tools.SecurityConstants;
import no.nav.common.utils.IdUtils;
import no.nav.log.MDCConstants;
import no.nav.sbl.config.AxsysConfig;
import no.nav.sbl.rest.RestUtils;
import no.nav.sbl.rest.domain.DecoratorDomain;
import no.nav.sbl.util.EnvironmentUtils;
import no.nav.tjenester.axsys.api.v1.HttpRequestConstants;
import org.slf4j.MDC;
import org.springframework.cache.annotation.Cacheable;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class AxsysService {
    private static final String AXSYS_URL = EnvironmentUtils.getRequiredProperty(AxsysConfig.AXSYS_URL);
    private static final String SYSTEMUSER = EnvironmentUtils.getRequiredProperty(SecurityConstants.SYSTEMUSER_USERNAME);

    private final WebTarget client = RestUtils.createClient().target(AXSYS_URL);
    private final WebTarget pingClient = RestUtils.createClient().target(AXSYS_URL.replace("/api", "/internal/isAlive"));

    @Cacheable("enheterCache")
    public Try<List<DecoratorDomain.Enhet>> hentEnheter(String ident) {
        return Try.of(() -> {
            String callId = Option.of(MDC.get(MDCConstants.MDC_CALL_ID)).getOrElse(IdUtils::generateId);
            String consumerId = Option.of(MDC.get(MDCConstants.MDC_CONSUMER_ID)).getOrElse(SYSTEMUSER);

            AxsysTilgangResponse response = client
                    .path("/v1/tilgang/" + ident)
                    .request()
                    .header(HttpRequestConstants.HEADER_NAV_CALL_ID, callId)
                    .header(HttpRequestConstants.HEADER_NAV_CONSUMER_ID, consumerId)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .get(AxsysTilgangResponse.class);

            return response.enheter
                    .stream()
                    .map((enhet) -> new DecoratorDomain.Enhet(enhet.getEnhetId(), enhet.getNavn()))
                    .collect(toList());
        });
    }

    public void ping() {
        int status = pingClient.request().get().getStatus();
        if (status != 200) {
            throw new RuntimeException("Axsys /isAlive status: " + status);
        }
    }
}
