package no.nav.sbl.db;

import no.nav.common.health.HealthCheck;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.selftest.SelfTestCheck;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.sbl.config.Pingable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


@Component
public class DbHelsesjekk implements HealthCheck, Pingable {

    private static final String MODIACONTEXTHOLDERDB_URL = "MODIACONTEXTHOLDERDB_URL";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public DbHelsesjekk(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public HealthCheckResult checkHealth() {
        try {
            helsesjekk();
            return HealthCheckResult.healthy();
        } catch (Exception exception) {
            return HealthCheckResult.unhealthy(exception);
        }
    }

    public void helsesjekk() {
        jdbcTemplate.execute("SELECT * FROM DUAL");
    }

    @Override
    public SelfTestCheck ping() {
        return new SelfTestCheck(
                "dbhelsesjekk " + EnvironmentUtils.getRequiredProperty(MODIACONTEXTHOLDERDB_URL),
                true,
                this::checkHealth
        );
    }
}
