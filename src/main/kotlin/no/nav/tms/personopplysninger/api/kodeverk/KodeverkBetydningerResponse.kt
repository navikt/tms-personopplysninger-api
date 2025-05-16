package no.nav.tms.personopplysninger.api.kodeverk

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate

class KodeverkBetydningerResponse(
    val betydninger: Map<String, List<Betydning>> = emptyMap()
) {

    fun tekst(key: String): String {
        return betydninger[key]
            ?.first()
            ?.tekst()
            ?: run {
                log.warn { "Feil ved utledning av kodeverkstekst for $key" }
                key
            }
    }

    fun term(key: String): String {
        return betydninger[key]
            ?.first()
            ?.term()
            ?: run {
                log.warn { "Feil ved utledning av kodeverkstekst for $key" }
                key
            }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}

data class Betydning(
    val gyldigFra: LocalDate,
    val gyldigTil: LocalDate,
    val beskrivelser: Map<String, Beskrivelse>
) {
    fun tekst(): String? {
        return beskrivelser[NORSK_BOKMAAL]?.tekst
    }

    fun term(): String? {
        return beskrivelser[NORSK_BOKMAAL]?.term
    }

    companion object {
        private const val NORSK_BOKMAAL = "nb"
    }
}

data class Beskrivelse(
    val term: String,
    val tekst: String? = null
)
