package no.nav.modiacontextholder.infrastructur

import no.nav.common.health.selftest.SelfTestCheck

interface HealthCheckAware {
    fun getHealthCheck(): SelfTestCheck
}
