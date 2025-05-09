package no.nav.tms.personopplysninger.api.personalia

import com.expediagroup.graphql.client.types.GraphQLClientResponse
import no.nav.pdl.generated.dto.HentTelefonQuery
import no.nav.pdl.generated.dto.henttelefonquery.Metadata
import no.nav.pdl.generated.dto.henttelefonquery.Person
import no.nav.pdl.generated.dto.henttelefonquery.Telefonnummer

object PdlResponseBuilder {
    fun hentTelefonnummerResponse(
        landskode: String,
        nummer: String,
        opplysningsId: String
    ) = HentTelefonResponse(
        HentTelefonQuery.Result(
            Person(listOf(
                Telefonnummer(
                    landskode = landskode,
                    nummer = nummer,
                    metadata = Metadata(
                        opplysningsId = opplysningsId
                    )
                )
            ))
        )
    )
}

class HentTelefonResponse(
    result: HentTelefonQuery.Result
): GraphQLClientResponse<HentTelefonQuery.Result> {
    override val errors = null
    override val extensions = null
    override val data = result
}
