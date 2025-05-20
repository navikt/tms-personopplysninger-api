package no.nav.tms.personopplysninger.api.kontoregister

import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.kontoregisterRoutes(kontoregisterConsumer: KontoregisterConsumer) {
    get("/land") {
        call.respond(kontoregisterConsumer.hentLandkoder())
    }

    get("/valuta") {
        call.respond(kontoregisterConsumer.hentValutakoder())
    }
}
