package no.nav.sbl.config;

import no.nav.common.featuretoggle.UnleashClient;
import org.springframework.beans.factory.annotation.Autowired;

public class FeatureToggle {

    private UnleashClient unleashService;

    @Autowired
    public FeatureToggle(UnleashClient unleashService) {
        this.unleashService = unleashService;
    }

}
