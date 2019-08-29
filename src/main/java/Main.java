import lombok.extern.slf4j.Slf4j;
import no.nav.apiapp.ApiApp;
import no.nav.sbl.config.ApplicationConfig;

import static no.nav.apiapp.rest.NavCorsFilter.CORS_ALLOWED_ORIGINS;
import static no.nav.sbl.util.EnvironmentUtils.EnviromentClass.P;
import static no.nav.sbl.util.EnvironmentUtils.EnviromentClass.Q;
import static no.nav.sbl.util.EnvironmentUtils.Type.PUBLIC;
import static no.nav.sbl.util.EnvironmentUtils.isEnvironmentClass;
import static no.nav.sbl.util.EnvironmentUtils.setProperty;

@Slf4j
public class Main {
    public static void main(String... args) {
        setCors();
        ApiApp.runApp(ApplicationConfig.class, args);
    }

    private static void setCors() {
        if (isEnvironmentClass(P)) {
            setProperty(CORS_ALLOWED_ORIGINS, ".adeo.no", PUBLIC);
        } else if (isEnvironmentClass(Q)) {
            String subDomain = ".preprod.local";
            log.info("Setting CORS-headers to {}, YOU SHOULD NOT SEE THIS IN PRODUCTION!!!", subDomain);
            setProperty(CORS_ALLOWED_ORIGINS, subDomain, PUBLIC);
        }
    }
}
