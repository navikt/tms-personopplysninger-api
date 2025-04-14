package no.nav.tms.personopplysninger.api.personalia;

import no.nav.pdl.generated.dto.HentPersonQuery
import no.nav.pdl.generated.dto.hentpersonquery.GeografiskTilknytning
import no.nav.tms.token.support.tokenx.validation.user.TokenXUser

class PersonaliaService(
//    private val kodeverkConsumer: KodeverkConsumer,
//    private val norg2Consumer: Norg2Consumer,
    private val kontoregisterConsumer: KontoregisterConsumer,
    private val pdlConsumer: PdlConsumer
) {

    suspend fun hentPersoninfo(user: TokenXUser): PersonaliaOgAdresser {
        return pdlConsumer.hentPerson(user).let { person ->
            val konto = kontoregisterConsumer.hentAktivKonto(user)
            person.toOutbound(
                konto = konto,
                kodeverk = createPersonaliaKodeverk(person, konto),
                enhetKontaktInformasjon = enhetKontaktInfoFor(person.geografiskTilknytning, token)
            )
        }
    }

    private suspend fun enhetKontaktInfoFor(
        geografiskTilknytning: GeografiskTilknytning?,
        token: String
    ): Norg2EnhetKontaktinfo? {
        return geografiskTilknytning.let { it?.gtBydel ?: it?.gtKommune }
            ?.let { norg2Consumer.hentEnhet(token, it) }
            ?.let { norg2Consumer.hentKontaktinfo(token, it.enhetNr) }
    }

    private suspend fun createPersonaliaKodeverk(
        inboundPdl: HentPersonQuery.Result,
        inboundKonto: Konto?
    ): PersonaliaKodeverk {
        return inboundPdl.person!!.run {
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
}
