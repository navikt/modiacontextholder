package no.nav.sbl.db;

import no.nav.apiapp.selftest.Helsesjekk;
import no.nav.apiapp.selftest.HelsesjekkMetadata;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Component
public class DbHelsesjekk implements Helsesjekk{
    private JdbcTemplate jdbcTemplate;

    @Inject
    public DbHelsesjekk(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void helsesjekk() throws Throwable {
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
