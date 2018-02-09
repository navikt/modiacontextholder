package no.nav.sbl.db;

import no.nav.apiapp.selftest.Helsesjekk;
import no.nav.apiapp.selftest.HelsesjekkMetadata;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Component
public class DbHelsesjekk implements Helsesjekk{
    private DataSource dataSource;

    @Inject
    public DbHelsesjekk(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void helsesjekk() throws Throwable {
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM PRODUCT_COMPONENT_VERSION");
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
    }

    @Override
    public HelsesjekkMetadata getMetadata() {
        return new HelsesjekkMetadata(
                "dbhelsesjekk",
                System.getProperty("db.url", "inmemory"),
                "Oracle-database",
                true
        );
    }
}
