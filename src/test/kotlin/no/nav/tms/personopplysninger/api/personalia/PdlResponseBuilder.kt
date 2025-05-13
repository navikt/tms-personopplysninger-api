package no.nav.tms.personopplysninger.api.personalia

object PdlResponseBuilder {
    fun hentTelefonnummerResponse(
        landskode: String,
        nummer: String,
        opplysningsId: String
    ) = """
{
    "data": {
        "person": {
            "telefonnummer": [
                {
                    "landskode": "$landskode",
                    "nummer": "$nummer",
                    "metadata": {
                        "opplysningsId": "$opplysningsId"
                    }
                }
            ]
        }
    }
}
    """.trimIndent()
}
