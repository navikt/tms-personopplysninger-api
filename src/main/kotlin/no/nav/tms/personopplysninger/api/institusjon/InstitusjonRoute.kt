package no.nav.tms.personopplysninger.api.institusjon

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.tms.personopplysninger.api.user

fun Route.institusjonRoute(institusjonConsumer: InstitusjonConsumer) {
    get("/institusjonsopphold") {
        call.respond(institusjonConsumer.hentInstitusjonsopphold(call.user))
    }
}
