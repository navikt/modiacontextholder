package no.nav.sbl.config

import no.nav.common.health.selftest.SelfTestCheck

interface Pingable {
    fun ping(): SelfTestCheck?
}
