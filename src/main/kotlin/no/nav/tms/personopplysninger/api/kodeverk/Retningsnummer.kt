package no.nav.tms.personopplysninger.api.kodeverk

data class Retningsnummer(
    val landskode: String,
    val land: String?,
) {
    companion object {
        fun mapKodeverkResponse(response: KodeverkBetydningerResponse): List<Retningsnummer> {
            return response.betydninger
                .map { (key, value) ->
                    Retningsnummer(
                        landskode = key,
                        land = value.first().tekst()
                    )
                }.sortedBy { it.land }
        }
    }
}
