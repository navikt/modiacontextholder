import org.slf4j.LoggerFactory

val log = LoggerFactory.getLogger("modiacontextholder.Application")

fun main() {
    val configuration = Configuration()
    val dbConfig = DataSourceConfiguration(configuration)

    DataSourceConfiguration.migrateDb(configuration, dbConfig.adminDataSource())

    HttpServer.create("modiacontextholder", 7070) {
        draftApp(
                configuration = configuration,
                dataSource = dbConfig.userDataSource(),
                useMock = false
        )
    }.start(wait = true)
}