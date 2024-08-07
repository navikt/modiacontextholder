package no.nav.sbl.service

import no.nav.sbl.redis.RedisPersistence
import no.nav.sbl.redis.TempCodeResult
import org.springframework.beans.factory.annotation.Autowired

class FnrCodeExchangeService(
    @Autowired private val redisPersistence: RedisPersistence,
) {
    suspend fun getFnr(code: String): Result<String?> = redisPersistence.getFnr(code)

    suspend fun generateAndStoreTempCodeForFnr(fnr: String): TempCodeResult = redisPersistence.generateAndStoreTempCodeForFnr(fnr)
}
