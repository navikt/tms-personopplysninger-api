package no.nav.tms.personopplysninger.api.kodeverk

import io.github.oshai.kotlinlogging.KotlinLogging

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
