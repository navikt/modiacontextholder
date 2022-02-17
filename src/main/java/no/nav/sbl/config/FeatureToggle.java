package no.nav.sbl.config;

import no.nav.common.featuretoggle.UnleashService;
import org.springframework.beans.factory.annotation.Autowired;

public class FeatureToggle {

    private UnleashService unleashService;

    @Autowired
    public FeatureToggle(UnleashService unleashService) {
        this.unleashService = unleashService;
    }

}
