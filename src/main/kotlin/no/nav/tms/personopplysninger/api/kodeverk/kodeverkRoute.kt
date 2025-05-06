package no.nav.tms.personopplysninger.api.kodeverk

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.kodeverkRoute(kodeverkConsumer: KodeverkConsumer) {
    get("/retningsnumre") {
        call.respond(kodeverkConsumer.hentRetningsnumre())
    }

    get("/postnummer") {
        call.respond(kodeverkConsumer.hentPostnummer())
    }
}
