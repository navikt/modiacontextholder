package no.nav.sbl.config;

import no.nav.sbl.db.DbHelsesjekk;
import no.nav.sbl.db.dao.EventDAO;
import no.nav.sbl.jdbc.DataSourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;

import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    public static final String MODIACONTEXTHOLDERDB_URL_PROPERTY = "MODIACONTEXTHOLDERDB_URL";
    public static final String MODIACONTEXTHOLDERDB_USERNAME = "MODIACONTEXTHOLDERDB_USERNAME";
    public static final String MODIACONTEXTHOLDERDB_PASSWORD = "MODIACONTEXTHOLDERDB_PASSWORD";

    @Bean
    public DataSource getDataSource() {
        return DataSourceFactory.dataSource()
                .url(getRequiredProperty(MODIACONTEXTHOLDERDB_URL_PROPERTY))
                .username(getRequiredProperty(MODIACONTEXTHOLDERDB_USERNAME))
                .password(getRequiredProperty(MODIACONTEXTHOLDERDB_PASSWORD))
                .maxPoolSize(300)
                .minimumIdle(1)
                .build();
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
