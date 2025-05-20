package no.nav.tms.personopplysninger.api.medl

import java.time.LocalDate

data class Medlemskapsunntak(
    val perioder: List<Medlemskapsperiode>
)

data class Medlemskapsperiode(
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate? = null,
    val medlem: Boolean,
    val hjemmel: String,
    val trygdedekning: String? = null,
    val lovvalgsland: String? = null,
    val kilde: String,
    val studieinformasjon: Studieinformasjon? = null
)

data class Studieinformasjon(
    val statsborgerland: String,
    val studieland: String?,
)
