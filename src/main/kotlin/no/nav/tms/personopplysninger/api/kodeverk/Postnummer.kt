package no.nav.tms.personopplysninger.api.kodeverk

data class Postnummer(
    val kode: String,
    val tekst: String? = null
) {
    companion object {
        fun mapKodeverkResponse(response: KodeverkBetydningerResponse): List<Postnummer> {
            return response.betydninger
                .map { (kode, betydninger) ->
                    Postnummer(
                        kode = kode,
                        tekst = betydninger.first().tekst()
                    )
                }.sortedBy { it.tekst }
        }
    }
}
