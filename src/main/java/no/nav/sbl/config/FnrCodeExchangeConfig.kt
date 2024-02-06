package no.nav.sbl.config

import no.nav.sbl.redis.RedisPersistence
import no.nav.sbl.service.FnrCodeExchangeService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class FnrCodeExchangeConfig {

    @Bean
    open fun fnrCodeExchangeService(redisPersistence: RedisPersistence): FnrCodeExchangeService = FnrCodeExchangeService(redisPersistence)
}
