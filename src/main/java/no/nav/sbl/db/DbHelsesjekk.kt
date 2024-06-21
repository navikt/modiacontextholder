package no.nav.sbl.db

import no.nav.common.health.HealthCheck
import no.nav.common.health.HealthCheckResult
import no.nav.common.health.selftest.SelfTestCheck
import no.nav.common.utils.EnvironmentUtils
import no.nav.sbl.config.Pingable
import org.springframework.jdbc.core.JdbcTemplate

class DbHelsesjekk(
    private val jdbcTemplate: JdbcTemplate,
) : HealthCheck,
    Pingable {
    override fun checkHealth(): HealthCheckResult =
        runCatching {
            jdbcTemplate.execute("SELECT * FROM DUAL")
        }.fold(
            onSuccess = { HealthCheckResult.healthy() },
            onFailure = { HealthCheckResult.unhealthy(it) },
        )

    override fun ping(): SelfTestCheck =
        SelfTestCheck(
            "dbhelsesjekk " + EnvironmentUtils.getRequiredProperty("MODIACONTEXTHOLDERDB_URL"),
            true,
        ) { this.checkHealth() }
}
