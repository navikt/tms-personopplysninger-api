package no.nav.tms.personopplysninger.api.personalia

import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.personopplysninger.api.user

fun Route.personalia(personaliaService: PersonaliaService) {
    get("/personalia") {
        call.respond(personaliaService.hentPersoninfo(call.user))
    }
}
