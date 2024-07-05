package no.nav.sbl.rest

import no.nav.sbl.service.FnrCodeExchangeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping(value = ["/api/fnr-code", "/modiacontextholder/api/fnr-code"])
class FnrCodeExchangeController(
    @Autowired private val fnrCodeExchangeService: FnrCodeExchangeService,
) {
    @PostMapping("/generate")
    suspend fun generateCodeForFnr(
        @RequestBody fnrRequest: FnrRequest,
    ): CodeResponse {
        val result = fnrCodeExchangeService.generateAndStoreTempCodeForFnr(fnrRequest.fnr)
        if (result.result.isFailure) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown error", result.result.exceptionOrNull())
        }
        return CodeResponse(fnr = fnrRequest.fnr, code = result.code)
    }

    @PostMapping("/retrieve")
    suspend fun fetchFnrWithCode(
        @RequestBody codeRequest: CodeRequest,
    ): CodeResponse {
        val result = fnrCodeExchangeService.getFnr(codeRequest.code)
        if (result.isFailure) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown error", result.exceptionOrNull())
        }
        val fnr = result.getOrNull() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke fnr for koden")
        return CodeResponse(fnr = fnr, code = codeRequest.code)
    }
}

data class FnrRequest(
    val fnr: String,
)

data class CodeRequest(
    val code: String,
)

data class CodeResponse(
    val fnr: String,
    val code: String,
)
