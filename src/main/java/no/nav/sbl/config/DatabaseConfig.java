package no.nav.sbl.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.sbl.db.DbHelsesjekk;
import no.nav.sbl.db.dao.EventDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

import java.util.List;
import java.util.Objects;

import static no.nav.common.utils.EnvironmentUtils.getRequiredProperty;


@Configuration
@EnableTransactionManagement
public class DatabaseConfig {
    public static final List<String> GCP_CLUSTERS = List.of("dev-gcp", "prod-gcp");
    public static final String MODIACONTEXTHOLDERDB_URL_PROPERTY = "MODIACONTEXTHOLDERDB_URL";
    public static final String MODIACONTEXTHOLDERDB_USERNAME = "MODIACONTEXTHOLDERDB_USERNAME";
    public static final String MODIACONTEXTHOLDERDB_PASSWORD = "MODIACONTEXTHOLDERDB_PASSWORD";

    @Bean
    public DataSource getDataSource() {
        String clusterName = String.valueOf(EnvironmentUtils.getClusterName());

        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(300);
        config.setMinimumIdle(1);

        if(GCP_CLUSTERS.contains(clusterName)){
            config.setJdbcUrl(getRequiredProperty("NAIS_DATABASE_MODIACONTEXTHOLDER_MODIACONTEXTHOLDER_DB_JDBC_URL"));
        } else {
            config.setJdbcUrl(getRequiredProperty(MODIACONTEXTHOLDERDB_URL_PROPERTY));
            config.setUsername(getRequiredProperty(MODIACONTEXTHOLDERDB_USERNAME));
            config.setPassword(getRequiredProperty(MODIACONTEXTHOLDERDB_PASSWORD));
        }

        return new HikariDataSource(config);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    public EventDAO eventDAO() {
        return new EventDAO();
    }

    @Bean
    public DbHelsesjekk dbHelsesjekk(JdbcTemplate jdbcTemplate) {
        return new DbHelsesjekk(jdbcTemplate);
    }
}
