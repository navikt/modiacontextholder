package no.nav.sbl.db;

import no.nav.apiapp.selftest.Helsesjekk;
import no.nav.apiapp.selftest.HelsesjekkMetadata;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Component
public class DbHelsesjekk implements Helsesjekk{

    private static final String MODIACONTEXTHOLDERDB_URL = "MODIACONTEXTHOLDERDB_URL";

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
                getRequiredProperty(MODIACONTEXTHOLDERDB_URL),
                "Oracle-database",
                true
        );
    }
}
