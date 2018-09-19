package no.nav.sbl.db;

import no.nav.apiapp.selftest.Helsesjekk;
import no.nav.apiapp.selftest.HelsesjekkMetadata;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class DbHelsesjekk implements Helsesjekk{
    private JdbcTemplate jdbcTemplate;

    @Inject
    public DbHelsesjekk(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void helsesjekk() {
        jdbcTemplate.execute("SELECT * FROM PRODUCT_COMPONENT_VERSION");
    }

    @Override
    public HelsesjekkMetadata getMetadata() {
        return new HelsesjekkMetadata(
                "dbhelsesjekk",
                System.getProperty("modiacontextholderDB.url", "inmemory"),
                "Oracle-database",
                true
        );
    }
}
