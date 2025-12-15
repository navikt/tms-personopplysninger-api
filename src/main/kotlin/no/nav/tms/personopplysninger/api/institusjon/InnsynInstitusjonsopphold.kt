package no.nav.tms.personopplysninger.api.institusjon

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.LocalDate
import java.time.LocalDateTime

data class InnsynInstitusjonsopphold (
    val organisasjonsnummer: String? = null,
    val institusjonsnavn: String? = null,
    val startdato: LocalDate,
    val faktiskSluttdato: LocalDate? = null,
    val fiktivSluttdato: Boolean? = null,
    val registreringstidspunkt: LocalDateTime,
    @param:JsonAlias("institusjonstype")
    private val institusjonstypekode: Institusjonstype? = null
) {
    val institusjonstype = institusjonstypekode?.tekst
}

enum class Institusjonstype(val tekst: String) {
    AS("Alders- og sykehjem"),
    FO("Fengsel"),
    HS("Helseinstitusjon")
}


