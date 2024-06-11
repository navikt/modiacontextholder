package no.nav.sbl.service.unleash;

import io.getunleash.UnleashContext;
import io.getunleash.UnleashContextProvider;
import no.nav.sbl.service.AuthContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UnleashContextProviderImpl implements UnleashContextProvider {

    private final AuthContextService authContextService;

    @Autowired
    public UnleashContextProviderImpl(AuthContextService authContextService) {
        this.authContextService = authContextService;
    }

    @Override
    public UnleashContext getContext() {
        String ident = authContextService.getIdent().orElse(null);
        String remoteAddr = null;

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            remoteAddr = attributes.getRequest().getRemoteAddr();
        } catch (Exception ignored) {

        }

        return UnleashContext.builder()
                .appName("modiacontextholder")
                .environment(System.getProperty("UNLEASH_ENVIRONMENT"))
                .userId(ident)
                .remoteAddress(remoteAddr)
                .build();
    }
}
