package no.nav.tms.personopplysninger.api.kontaktinformasjon

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.tms.personopplysninger.api.user

fun Route.kontaktinformasjon(kontaktinformasjonService: KontaktinformasjonService) {
    get("/kontaktinformasjon") {
        call.respond(kontaktinformasjonService.hentKontaktinformasjon(call.user))
    }
}
