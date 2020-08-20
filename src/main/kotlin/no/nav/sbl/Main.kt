package no.nav.sbl

import Configuration
import DataSourceConfiguration
import HttpServer
import org.slf4j.LoggerFactory

val log = LoggerFactory.getLogger("no.nav.sbl.modiacontextholder.Application")

fun main() {
    val configuration = Configuration();
    val dbConfig = DataSourceConfiguration(configuration)

    DataSourceConfiguration.migrateDb(configuration, dbConfig.adminDataSource())

    HttpServer.create("no.nav.sbl.modiacontextholder", 7070) {
        modiacontextholder(
                configuration = configuration,
                dataSource = dbConfig.userDataSource()
        )
    }.start(wait = true)
}