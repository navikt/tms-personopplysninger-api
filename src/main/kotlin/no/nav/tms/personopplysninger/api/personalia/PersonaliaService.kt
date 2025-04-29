package no.nav.tms.personopplysninger.api.personalia;

import no.nav.pdl.generated.dto.hentpersonquery.GeografiskTilknytning
import no.nav.pdl.generated.dto.hentpersonquery.Statsborgerskap
import no.nav.tms.personopplysninger.api.UserPrincipal
import no.nav.tms.personopplysninger.api.kodeverk.AdresseKodeverk
import no.nav.tms.personopplysninger.api.kodeverk.KodeverkConsumer
import no.nav.tms.personopplysninger.api.kodeverk.PersonaliaKodeverk
import no.nav.tms.personopplysninger.api.personalia.addresse.kommunenummer
import no.nav.tms.personopplysninger.api.personalia.addresse.landkode
import no.nav.tms.personopplysninger.api.personalia.addresse.postnummer
import no.nav.tms.personopplysninger.api.personalia.norg2.Norg2Consumer
import no.nav.tms.personopplysninger.api.personalia.norg2.Norg2EnhetKontaktinfo
import no.nav.pdl.generated.dto.hentpersonquery.Person as PdlPerson
import java.time.LocalDate

class PersonaliaService(
    private val kodeverkConsumer: KodeverkConsumer,
    private val norg2Consumer: Norg2Consumer,
    private val kontoregisterConsumer: KontoregisterConsumer,
    private val pdlConsumer: PdlConsumer
) {
    suspend fun hentPersoninfo(user: UserPrincipal): PersonaliaOgAdresser {
        return pdlConsumer.hentPerson(user).let { result ->
            val person = requireNotNull(result.person)

            val konto = kontoregisterConsumer.hentAktivKonto(user)
            PersonaliaOgAdresser.mapResult(
                person = person,
                konto = konto,
                kodeverk = createPersonaliaKodeverk(person, konto),
                enhetKontaktInformasjon = enhetKontaktInfoFor(result.geografiskTilknytning)
            )
        }
    }

    private suspend fun enhetKontaktInfoFor(
        geografiskTilknytning: GeografiskTilknytning?
    ): Norg2EnhetKontaktinfo? {
        return geografiskTilknytning.let { it?.gtBydel ?: it?.gtKommune }
            ?.let { norg2Consumer.hentEnhet(it) }
            ?.let { norg2Consumer.hentKontaktinfo(it.enhetNr) }
    }

    private suspend fun createPersonaliaKodeverk(
        person: PdlPerson,
        inboundKonto: Konto?
    ): PersonaliaKodeverk {
        return with(person) {
            PersonaliaKodeverk(
                foedekommuneterm = hentKommuneKodeverksTerm(foedested.firstOrNull()?.foedekommune),
                foedelandterm = hentLandKodeverksTerm(foedested.firstOrNull()?.foedeland),
                statsborgerskaptermer = hentGyldigeStatsborgerskap(statsborgerskap),
                utenlandskbanklandterm = inboundKonto?.utenlandskKontoInfo?.let {
                    hentLandKontoregisterKodeterm(it.bankLandkode)
                },
                utenlandskbankvalutaterm = inboundKonto?.utenlandskKontoInfo?.let {
                    hentValutaKontoregisterKodeterm(it.valutakode)
                },
                kontaktadresseKodeverk = kontaktadresse.map {
                    hentAdresseKodeverk(it.postnummer, it.landkode, it.kommunenummer)
                },
                bostedsadresseKodeverk = bostedsadresse.firstOrNull().let {
                    hentAdresseKodeverk(it?.postnummer, it?.landkode, it?.kommunenummer)
                },
                deltBostedKodeverk = deltBosted.firstOrNull().let {
                    hentAdresseKodeverk(it?.postnummer, it?.landkode, it?.kommunenummer)
                },
                oppholdsadresseKodeverk = oppholdsadresse.map {
                    hentAdresseKodeverk(it.postnummer, it.landkode, it.kommunenummer)
                })
        }
    }

    private suspend fun hentGyldigeStatsborgerskap(statsborgerskap: List<Statsborgerskap>): List<String> {
        return statsborgerskap
            .filter { it.land != UKJENT_LAND && it.isValid() } // Filtrer ut ukjent og ugyldige
            .map { kodeverkConsumer.hentStatsborgerskap().term(it.land) }
            .filter { it.isNotEmpty() }
    }

    private fun Statsborgerskap.isValid(): Boolean {
        return this.gyldigTilOgMed.let { it == null || LocalDate.parse(it).isAfter(LocalDate.now()) }
    }

    private suspend fun hentAdresseKodeverk(
        postnummer: String?,
        landkode: String?,
        kommunenummer: String?
    ): AdresseKodeverk {
        return AdresseKodeverk(
            poststed = postnummer?.let { kodeverkConsumer.hentPostnummer().term(it) },
            land = landkode?.let { kodeverkConsumer.hentLandKoder().term(it) },
            kommune = hentKommuneKodeverksTerm(kommunenummer),
        )
    }

    private suspend fun hentLandKodeverksTerm(inbound: String?): String? {
        return inbound?.let { kodeverkConsumer.hentLandKoder().term(it) }
    }

    private suspend fun hentKommuneKodeverksTerm(inbound: String?): String? {
        return if ("0000" == inbound) {
            ""
        } else {
            inbound?.let { kodeverkConsumer.hentKommuner().term(it) }
        }
    }

    private suspend fun hentValutaKontoregisterKodeterm(kode: String?): String? {
        return kontoregisterConsumer.hentValutakoder().find { it.valutakode == kode }?.valuta
    }

    private suspend fun hentLandKontoregisterKodeterm(kode: String?): String? {
        return kontoregisterConsumer.hentLandkoder().find { it.landkode == kode }?.land
    }

    companion object {
        private const val UKJENT_LAND = "XUK"
    }
}
