package no.nav.sbl.config;

import no.nav.sbl.db.DbHelsesjekk;
import no.nav.sbl.db.dao.EventDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        JndiDataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
        dataSourceLookup.setResourceRef(true);
        return dataSourceLookup.getDataSource("jdbc/modiacontextholderDS");
    }

    @Bean
    public JdbcTemplate jdbcTemplate() throws NamingException, SQLException, IOException {
        return new JdbcTemplate(dataSource());
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager() throws NamingException {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(dataSource());
    }

    @Bean
    public EventDAO eventDAO() {
        return new EventDAO();
    }

    @Bean
    public DbHelsesjekk dbHelsesjekk(DataSource dataSource) {
        return new DbHelsesjekk(dataSource);
    }
}
