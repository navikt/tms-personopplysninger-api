package no.nav.tms.personopplysninger.api.sporingslogg

import io.ktor.server.routing.*
import io.ktor.server.response.respond
import no.nav.tms.personopplysninger.api.user

fun Route.sporingsloggRoutes(service: SporingsloggService) {
    get("/sporingslogg") {
        call.respond(service.hentSporingslogg(call.user))
    }
}
