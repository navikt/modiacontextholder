import no.nav.sbl.dialogarena.common.jetty.Jetty;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

import static no.nav.sbl.dialogarena.common.jetty.Jetty.usingWar;
import static no.nav.sbl.dialogarena.common.jetty.JettyStarterUtils.*;
import static no.nav.sbl.dialogarena.test.SystemProperties.setFrom;

public class StartJetty {
    public static void main(String[] args) throws Exception {
        setFrom("environment.properties");

        Jetty jetty = usingWar()
                .at("modiacontextholder")
                .port(8390)
                .overrideWebXml()
                .addDatasource(buildDataSource("oracledb.properties"), "jdbc/modiacontextholderDS")
                .buildJetty();
        jetty.startAnd(first(waitFor(gotKeypress())).then(jetty.stop));
    }

    public static DataSource buildDataSource(String propertyFileName) throws IOException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        Properties env = dbProperties(propertyFileName);
        dataSource.setDriverClassName(env.getProperty("db.driverClassName"));
        dataSource.setUrl(env.getProperty("db.url"));
        dataSource.setUsername(env.getProperty("db.username"));
        dataSource.setPassword(env.getProperty("db.password"));
        return dataSource;
    }

    private static Properties dbProperties(String propertyFileName) throws IOException {
        Properties env = new Properties();
        env.load(StartJetty.class.getResourceAsStream("/" + propertyFileName));
        return env;
    }

}
