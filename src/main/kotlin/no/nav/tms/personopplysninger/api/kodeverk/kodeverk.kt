package no.nav.tms.personopplysninger.api.kodeverk

data class AdresseKodeverk (
    val poststed: String? = null,
    val land: String? = null,
    val kommune: String? = null,
)

class PersonaliaKodeverk(
    val foedelandterm: String? = null,
    val foedekommuneterm: String? = null,
    val statsborgerskaptermer: List<String> = emptyList(),
    val utenlandskbanklandterm: String? = null,
    val utenlandskbankvalutaterm: String? = null,
    val kontaktadresseKodeverk: List<AdresseKodeverk> = emptyList(),
    val bostedsadresseKodeverk: AdresseKodeverk = AdresseKodeverk(),
    val deltBostedKodeverk: AdresseKodeverk = AdresseKodeverk(),
    val oppholdsadresseKodeverk: List<AdresseKodeverk> = emptyList(),
)
