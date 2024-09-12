package no.nav.modiacontextholder.consumers.pdl.generated

import com.expediagroup.graphql.client.Generated
import com.expediagroup.graphql.client.types.GraphQLClientRequest
import kotlin.String
import kotlin.reflect.KClass
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import no.nav.modiacontextholder.consumers.pdl.generated.hentident.Identliste

public const val HENT_IDENT: String =
    "query(${'$'}ident: ID!){\n    hentIdenter(ident: ${'$'}ident, grupper: [AKTORID]) {\n        identer {\n            ident\n        }\n    }\n}"

@Generated
@Serializable
public class HentIdent(
  override val variables: HentIdent.Variables,
) : GraphQLClientRequest<HentIdent.Result> {
  @Required
  override val query: String = HENT_IDENT

  override fun responseType(): KClass<HentIdent.Result> = HentIdent.Result::class

  @Generated
  @Serializable
  public data class Variables(
    public val ident: ID,
  )

  @Generated
  @Serializable
  public data class Result(
    public val hentIdenter: Identliste? = null,
  )
}
