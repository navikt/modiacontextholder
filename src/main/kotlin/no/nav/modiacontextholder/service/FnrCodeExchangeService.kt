package no.nav.modiacontextholder.service

import no.nav.modiacontextholder.valkey.ValkeyPersistence
import no.nav.modiacontextholder.valkey.TempCodeResult

class FnrCodeExchangeService(
    private val valkeyPersistence: ValkeyPersistence,
) {
    suspend fun getFnr(code: String): Result<String?> = valkeyPersistence.getFnr(code)

    suspend fun generateAndStoreTempCodeForFnr(fnr: String): TempCodeResult = valkeyPersistence.generateAndStoreTempCodeForFnr(fnr)
}
