package no.nav.sbl.service.unleash;

import io.getunleash.Unleash;

public class UnleashServiceImpl implements UnleashService {
    private final Unleash defaultUnleash;

    public UnleashServiceImpl(Unleash defaultUnleash) {
        this.defaultUnleash = defaultUnleash;
    }

    @Override
    public boolean isEnabled(Feature feature) {
        return defaultUnleash.isEnabled(feature.getPropertyKey());
    }

    @Override
    public boolean isEnabled(String feature) {
        return defaultUnleash.isEnabled(feature);
    }
}
