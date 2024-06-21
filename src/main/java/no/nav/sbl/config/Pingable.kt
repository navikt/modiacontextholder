package no.nav.sbl.config;

import no.nav.common.health.selftest.SelfTestCheck;

public interface Pingable {
    public SelfTestCheck ping();
}
