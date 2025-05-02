package no.nav.tms.personopplysninger.api.medl

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.tms.personopplysninger.api.user

fun Route.medl(medlService: MedlService) {
    get("/medl") {
        call.respond(medlService.hentMedlemskap(call.user))
    }
}
