package no.nav.tms.personopplysninger.api.kodeverk

data class Postnummer(
    val kode: String,
    val tekst: String? = null
) {
    companion object {
        fun mapKodeverkResponse(response: KodeverkBetydningerResponse): List<Postnummer> {
            return response.betydninger
                .map { (key, value) ->
                    Postnummer(
                        kode = key,
                        tekst = value.first().tekst()
                    )
                }.sortedBy { it.tekst }
        }
    }
}
