package no.nav.tms.personopplysninger.api.personalia

import no.nav.tms.personopplysninger.api.kodeverk.PersonaliaKodeverk
import no.nav.tms.personopplysninger.api.personalia.addresse.Adresser
import no.nav.tms.personopplysninger.api.personalia.norg2.Norg2EnhetKontaktinfo

import no.nav.pdl.generated.dto.hentpersonquery.Person as PdlPerson

data class PersonaliaOgAdresser(
    val personalia: Personalia,
    val adresser: Adresser?,
    val enhetKontaktInformasjon: Norg2EnhetKontaktinfo?
) {
    companion object {
        fun mapResult(
            person: PdlPerson,
            konto: Konto?,
            kodeverk: PersonaliaKodeverk,
            enhetKontaktInformasjon: Norg2EnhetKontaktinfo?
        ): PersonaliaOgAdresser {

            return PersonaliaOgAdresser(
                personalia = Personalia.mapPersonalia(
                    person,
                    konto,
                    kodeverk
                ),
                adresser = Adresser.mapAdresser(
                    person,
                    kodeverk
                ),
                enhetKontaktInformasjon
            )
        }
    }
}
