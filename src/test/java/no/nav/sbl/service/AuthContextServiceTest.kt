package no.nav.sbl.service

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.PlainJWT
import no.nav.common.auth.context.AuthContext
import no.nav.common.auth.context.UserRole
import no.nav.common.client.msgraph.MsGraphClient
import no.nav.sbl.util.AuthContextUtils.withAccesstoken
import no.nav.sbl.util.AuthContextUtils.withContext
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AuthContextServiceTest {
    @Mock
    var client: MsGraphClient? = null

    @InjectMocks
    var authContextService: AuthContextService? = null

    @Test
    fun skal_hente_ident_fra_obo() {
        withContext(AAD_OBO_CONTEXT) {
            Assertions.assertThat(authContextService!!.ident).hasValue(AAD_OBO_IDENT)
            Mockito.verify(client, Mockito.never())?.hentOnPremisesSamAccountName(ArgumentMatchers.anyString())
            Mockito.verify(client, Mockito.never())?.hentUserData(ArgumentMatchers.anyString())
        }
    }

    @Test
    fun skal_hente_ident_fra_graph_api_om_accesstoken_eksisterer() {
        Mockito.`when`(client!!.hentOnPremisesSamAccountName(ArgumentMatchers.eq(AAD_ACCESS_TOKEN))).thenReturn(
            AAD_OBO_IDENT,
        )
        withAccesstoken(AAD_ACCESS_TOKEN) {
            Assertions.assertThat(authContextService!!.ident).hasValue(AAD_OBO_IDENT)
            Mockito.verify(client, Mockito.times(1))?.hentOnPremisesSamAccountName(ArgumentMatchers.anyString())
            Mockito.verify(client, Mockito.never())?.hentUserData(ArgumentMatchers.anyString())
        }
    }

    companion object {
        private const val AAD_OBO_IDENT = "Z999991"
        private const val AAD_ACCESS_TOKEN = "add-access-token-here"
        private val AAD_OBO_CONTEXT =
            AuthContext(
                UserRole.INTERN,
                PlainJWT(JWTClaimsSet.Builder().claim(AuthContextService.AAD_NAV_IDENT_CLAIM, AAD_OBO_IDENT).build()),
            )
    }
}
