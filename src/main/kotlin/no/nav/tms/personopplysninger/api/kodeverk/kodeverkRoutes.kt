package no.nav.tms.personopplysninger.api.kodeverk

import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.kodeverkRoutes(kodeverkConsumer: KodeverkConsumer) {
    get("/retningsnumre") {
        kodeverkConsumer.hentRetningsnumre().let {
            Retningsnummer.mapKodeverkResponse(it)
        }.let {
            call.respond(it)
        }
    }

    get("/postnummer") {
        kodeverkConsumer.hentPostnummer().let {
            Postnummer.mapKodeverkResponse(it)
        }.let {
            call.respond(it)
        }
    }
}
