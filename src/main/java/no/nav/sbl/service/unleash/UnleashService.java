package no.nav.sbl.service.unleash;


public interface UnleashService {

    boolean isEnabled(Feature feature);

    boolean isEnabled(String feature);
}
