package no.nav.tms.personopplysninger.api.medl

import no.nav.tms.personopplysninger.api.UserPrincipal
import no.nav.tms.personopplysninger.api.kodeverk.KodeverkConsumer

class MedlService(
    private val medlConsumer: MedlConsumer,
    private val kodeverkConsumer: KodeverkConsumer
) {
    suspend fun hentMedlemskap(user: UserPrincipal): Medlemskapsunntak {
        return medlConsumer.hentMedlemskap(user)
            .perioder
            .map { periode ->
                periode.copy(
                    hjemmel = hjemmelKodeverk(periode),
                    trygdedekning = trygdedekningKodeverk(periode),
                    lovvalgsland = lovvalgslandKodeverk(periode),
                    studieinformasjon = periode.studieinformasjon?.copy(
                        statsborgerland = statsborgerlandKodeverk(periode.studieinformasjon),
                        studieland = studielandKodeverk(periode.studieinformasjon)
                    )
                )
            }.let { perioder -> Medlemskapsunntak(perioder) }
    }

    private suspend fun trygdedekningKodeverk(periode: Medlemskapsperiode): String? {
        return periode.trygdedekning?.let { kodeverkConsumer.hentDekningMedl().tekst(it) }
    }

    private suspend fun hjemmelKodeverk(periode: Medlemskapsperiode): String {
        return kodeverkConsumer.hentGrunnlagMedl().tekst(periode.hjemmel)
    }

    private suspend fun lovvalgslandKodeverk(periode: Medlemskapsperiode): String? {
        return periode.lovvalgsland?.let { kodeverkConsumer.hentLandKoder().term(it) }
    }

    private suspend fun statsborgerlandKodeverk(studieinfo: Studieinformasjon): String {
        return studieinfo.statsborgerland.let { kodeverkConsumer.hentLandKoder().term(it) }
    }

    private suspend fun studielandKodeverk(studieinfo: Studieinformasjon): String? {
        return studieinfo.studieland?.let { kodeverkConsumer.hentLandKoder().term(it) }
    }
}

