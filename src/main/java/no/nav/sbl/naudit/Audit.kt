package no.nav.sbl.naudit

import no.nav.sbl.naudit.AuditIdentifier.FAIL_REASON
import no.nav.sbl.naudit.AuditIdentifier.DENY_REASON
import org.slf4j.LoggerFactory
import java.util.*

val tjenestekallLogg = LoggerFactory.getLogger("SecureLog")
class Audit {
    open class AuditResource(val resource: String)
    enum class Action {
        CREATE, READ, UPDATE, DELETE
    }

    interface AuditDescriptor<T> {
        fun log(resource: T?)
        fun denied(reason: String)
        fun failed(exception: Throwable)

        fun Throwable.getFailureReason(): String = this.message ?: this.toString()
    }

    internal class ParameterizedDescriptor<T>(
        private val subject: Optional<String>,
        private val action: Action,
        private val resourceType: AuditResource,
        private val extractIdentifiers: (T?) -> List<Pair<AuditIdentifier, String?>>
    ) : AuditDescriptor<T> {
        override fun log(resource: T?) {
            val identifiers = extractIdentifiers(resource).toTypedArray()
            logInternal(subject, action, resourceType, identifiers)
        }

        override fun denied(reason: String) {
            logInternal(subject, action, resourceType, arrayOf(DENY_REASON to reason))
        }

        override fun failed(exception: Throwable) {
            logInternal(subject, action, resourceType, arrayOf(FAIL_REASON to exception.getFailureReason()))
        }
    }

    internal class NoopDescriptor<T> : AuditDescriptor<T> {
        override fun log(resource: T?) {}
        override fun denied(reason: String) {}
        override fun failed(exception: Throwable) {}
    }

    internal class Descriptor(
        private val subject: Optional<String>,
        private val action: Action,
        private val resourceType: AuditResource,
        private val identifiers: Array<out Pair<AuditIdentifier, String?>>
    ) : AuditDescriptor<Any> {
        override fun log(resource: Any?) {
            logInternal(subject, action, resourceType, identifiers)
        }

        override fun denied(reason: String) {
            logInternal(subject, action, resourceType, arrayOf(DENY_REASON to reason))
        }

        override fun failed(exception: Throwable) {
            logInternal(subject, action, resourceType, arrayOf(FAIL_REASON to exception.getFailureReason()))
        }
    }

    companion object {
        val skipAuditLog: AuditDescriptor<Any> = NoopDescriptor()

        @JvmStatic
        fun <T> skipAuditLog(): AuditDescriptor<T> = NoopDescriptor()

        @JvmStatic
        fun describe(subject: Optional<String>, action: Action, resourceType: AuditResource, vararg identifiers: Pair<AuditIdentifier, String?>): AuditDescriptor<Any> {
            return Descriptor(subject, action, resourceType, identifiers)
        }

        @JvmStatic
        fun <T> describe(subject: Optional<String>, action: Action, resourceType: AuditResource, extractIdentifiers: (T?) -> List<Pair<AuditIdentifier, String?>>): AuditDescriptor<T> {
            return ParameterizedDescriptor(subject, action, resourceType, extractIdentifiers)
        }

        @JvmStatic
        fun <S> withAudit(descriptor: AuditDescriptor<in S>, supplier: () -> S): S {
            return runCatching(supplier)
                .onSuccess(descriptor::log)
                .onFailure(descriptor::failed)
                .getOrThrow()
        }

        private fun logInternal(subject: Optional<String>, action: Action, resourceType: AuditResource, identifiers: Array<out Pair<AuditIdentifier, String?>>) {
            val logline = listOfNotNull(
                "action='$action'",
                subject.map { "subject='$it'" }.orElse(null),
                "resource='${resourceType.resource}'",
                *identifiers
                    .map { "${it.first}='${it.second ?: "-"}'" }
                    .toTypedArray()
            )
                .joinToString(" ")

            tjenestekallLogg.info(logline)
        }
    }
}