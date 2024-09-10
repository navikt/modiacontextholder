package no.nav.sbl

import no.nav.common.nais.NaisYamlUtils
import no.nav.common.rest.client.RestClient
import no.nav.common.test.ssl.SSLTestUtils
import no.nav.common.test.ssl.TrustAllSSLSocketFactory
import org.springframework.boot.SpringApplication
import java.io.IOException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

object MainTest {
    init {
        setupRestClient()
        val naisConfig =
            NaisYamlUtils.getTemplatedConfig(
                ".nais/dev.yaml",
                object : HashMap<Any?, Any?>() {
                    init {
                        put("namespace", "q0")
                    }
                },
            )
        NaisYamlUtils.loadFromYaml(naisConfig)
        SSLTestUtils.disableCertificateChecks()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val application = SpringApplication(Main::class.java)
        application.setAdditionalProfiles("local")
        application.run(*args)
    }

    private fun setupRestClient() {
        RestClient.setBaseClient(
            RestClient
                .baseClientBuilder()
                .sslSocketFactory(
                    TrustAllSSLSocketFactory(),
                    object : X509TrustManager {
                        @Throws(CertificateException::class)
                        override fun checkClientTrusted(
                            x509Certificates: Array<X509Certificate>,
                            s: String,
                        ) {
                        }

                        @Throws(CertificateException::class)
                        override fun checkServerTrusted(
                            x509Certificates: Array<X509Certificate>,
                            s: String,
                        ) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
                    },
                ).build(),
        )
    }
}
