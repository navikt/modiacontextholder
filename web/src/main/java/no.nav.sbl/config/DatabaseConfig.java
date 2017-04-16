package no.nav.sbl.config;

import no.nav.sbl.db.dao.EventDAO;
import no.nav.sbl.db.domain.PEvent;
import no.nav.sbl.dialogarena.types.Pingable;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        JndiDataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
        dataSourceLookup.setResourceRef(true);
        return dataSourceLookup.getDataSource("jdbc/modiacontextholderDS");
    }

    @Bean(name = "sessionFactory")
    public SessionFactory sessionFactory() {
        LocalSessionFactoryBuilder sessionBuilder = new LocalSessionFactoryBuilder(dataSource());
        sessionBuilder.addAnnotatedClasses(PEvent.class);
        sessionBuilder.addProperties(hibernateProperties());
        return sessionBuilder.buildSessionFactory();
    }

    @Bean
    public HibernateTransactionManager transactionManager() {
        HibernateTransactionManager txManager = new HibernateTransactionManager();
        txManager.setSessionFactory(sessionFactory());
        return txManager;
    }

    @Bean
    public EventDAO eventDAO() {
        return new EventDAO();
    }

    private Properties hibernateProperties() {
        Properties hibernate = new Properties();
        hibernate.setProperty("hibernate.dialect", "org.hibernate.dialect.Oracle10gDialect");
        hibernate.setProperty("hibernate.show_sql", "false");
        hibernate.setProperty("hibernate.format_sql", "true");
        hibernate.setProperty("hibernate.jdbc.fetch_size", "100");
        return hibernate;
    }

    @Bean
    public Pingable dbPing() {
        return () -> {
            try (Connection connection = dataSource().getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM PRODUCT_COMPONENT_VERSION");
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return Pingable.Ping.lyktes("DATABASE");
            } catch (Exception e) {
                return Pingable.Ping.feilet("DATABASE", e);
            }
        };
    }
}
