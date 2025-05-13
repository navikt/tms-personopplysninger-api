package no.nav.tms.personopplysninger.api.kodeverk

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate

class KodeverkBetydningerResponse {
    val betydninger: Map<String, List<Betydning>> = emptyMap()

    fun tekst(key: String): String {
        return betydninger[key]?.first()?.tekst()
            ?: key.also {
                log.warn { "Feil ved utledning av kodeverkstekst for $key" }
            }
    }

    fun term(key: String): String {
        return betydninger[key]?.first()?.term()
            ?: key.also {
                log.warn { "Feil ved utledning av kodeverksterm for $key" }
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

data class Beskrivelse(
    val term: String,
    val tekst: String? = null
)
