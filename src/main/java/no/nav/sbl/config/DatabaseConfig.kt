package no.nav.sbl.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.common.utils.EnvironmentUtils
import no.nav.sbl.db.DbHelsesjekk
import no.nav.sbl.db.dao.EventDAO
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
open class DatabaseConfig {
    companion object {
        val GCP_CLUSTERS = listOf("dev-gcp", "prod-gcp")
        const val MODIACONTEXTHOLDERDB_URL_PROPERTY = "MODIACONTEXTHOLDERDB_URL"
        const val MODIACONTEXTHOLDERDB_USERNAME = "MODIACONTEXTHOLDERDB_USERNAME"
        const val MODIACONTEXTHOLDERDB_PASSWORD = "MODIACONTEXTHOLDERDB_PASSWORD"
    }

    @Bean
    open fun getDataSource(): DataSource {
        val clusterName = EnvironmentUtils.getRequiredProperty("NAIS_CLUSTER_NAME")

        val config = HikariConfig()

        if (GCP_CLUSTERS.contains(clusterName)) {
            config.jdbcUrl =
                EnvironmentUtils.getRequiredProperty("NAIS_DATABASE_MODIACONTEXTHOLDER_MODIACONTEXTHOLDER_DB_JDBC_URL")
        } else {
            config.jdbcUrl = EnvironmentUtils.getRequiredProperty(MODIACONTEXTHOLDERDB_URL_PROPERTY)
            config.username = EnvironmentUtils.getRequiredProperty(MODIACONTEXTHOLDERDB_USERNAME)
            config.password = EnvironmentUtils.getRequiredProperty(MODIACONTEXTHOLDERDB_PASSWORD)
        }
        config.maximumPoolSize = 300
        config.minimumIdle = 1

        val dataSource = HikariDataSource(config)

        if (GCP_CLUSTERS.contains(clusterName)) {
            Flyway
                .configure()
                .dataSource(dataSource)
                .load()
                .migrate()
        }

        return dataSource
    }

    @Bean
    open fun jdbcTemplate(dataSource: DataSource): JdbcTemplate = JdbcTemplate(dataSource)

    @Bean(name = ["transactionManager"])
    open fun transactionManager(dataSource: DataSource): PlatformTransactionManager = DataSourceTransactionManager(dataSource)

    @Bean
    open fun namedParameterJdbcTemplate(dataSource: DataSource): NamedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)

    @Bean
    @Autowired
    open fun eventDAO(
        jdbcTemplate: JdbcTemplate,
        namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
        applicationCluster: ApplicationCluster,
    ): EventDAO = EventDAO(jdbcTemplate, namedParameterJdbcTemplate, applicationCluster)

    @Bean
    open fun dbHelsesjekk(jdbcTemplate: JdbcTemplate): DbHelsesjekk = DbHelsesjekk(jdbcTemplate)
}
