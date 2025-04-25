package no.nav.tms.personopplysninger.api.kodeverk

import java.time.LocalDate

data class Betydning(
    val gyldigFra: LocalDate,
    val gyldigTil: LocalDate,
    val beskrivelser: Map<String, Beskrivelse>
) {
    fun tekst(lang: String = NORSK_BOKMAAL): String? {
        return beskrivelser[lang]?.tekst
    }

    fun term(lang: String = NORSK_BOKMAAL): String? {
        return beskrivelser[lang]?.term
    }

    companion object {
        private const val NORSK_BOKMAAL = "nb"
    }
}
