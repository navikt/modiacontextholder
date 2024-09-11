package no.nav.modiacontextholder

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

object AppModule {
    val appModule =
        module {
            singleOf < Unleash()
        }
}
