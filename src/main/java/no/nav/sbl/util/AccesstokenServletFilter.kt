package no.nav.sbl.util

import no.nav.common.auth.utils.CookieUtils
import no.nav.common.utils.fn.UnsafeRunnable
import java.util.*
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

class AccesstokenServletFilter : Filter {
    interface AccesstokenHolder {
        fun withAccesstoken(token: String?, runnable: UnsafeRunnable)
        fun requireAccesstoken(): String
        fun getAccesstoken(): Optional<String>
    }
    class AccesstokenHolderThreadLocal private constructor() : AccesstokenHolder {
        companion object {
            private val tokenholder = ThreadLocal<String?>()
            private val instance: AccesstokenHolder = AccesstokenHolderThreadLocal()

            fun instance(): AccesstokenHolder = instance
        }

        override fun withAccesstoken(token: String?, runnable: UnsafeRunnable) {
            val previous = tokenholder.get()
            try {
                tokenholder.set(token)
                runnable.run()
            } finally {
                tokenholder.set(previous)
            }
        }

        override fun requireAccesstoken(): String {
            return requireNotNull(tokenholder.get())
        }

        override fun getAccesstoken(): Optional<String> {
            return Optional.ofNullable(tokenholder.get())
        }
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val token: String? = CookieUtils.getCookieValue(
            AZURE_AD_ACCESS_TOKEN_COOKIE_NAME,
            request as HttpServletRequest
        ).orElse(null)

        AccesstokenHolderThreadLocal.instance().withAccesstoken(token) {
            chain.doFilter(request, response)
        }
    }

    companion object {
        @JvmStatic
        val AZURE_AD_ACCESS_TOKEN_COOKIE_NAME = "isso-accesstoken"
    }
}