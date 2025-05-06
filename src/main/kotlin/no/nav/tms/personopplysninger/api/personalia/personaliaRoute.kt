package no.nav.tms.personopplysninger.api.personalia

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.personopplysninger.api.user

fun Route.personalia(personaliaService: HentPersonaliaService, oppdaterPersonaliaService: OppdaterPersonaliaService) {
    get("/personalia") {
        call.respond(personaliaService.hentPersoninfo(call.user))
    }

    post("/endreTelefonnummer") {
        val telefonnummer = call.receive<TelefonnummerEndring>()

        val resp = oppdaterPersonaliaService.endreTelefonnummer(call.user, telefonnummer)
        call.respond(resp)
    }

    post("/slettTelefonnummer") {
        val telefonnummer = call.receive<TelefonnummerEndring>()

        val resp = oppdaterPersonaliaService.slettTelefonNummer(call.user, telefonnummer)
        call.respond(resp)
    }
}

data class TelefonnummerEndring(
    val landskode: String,
    val nummer: String,
    val prioritet: Int? = 1
)
