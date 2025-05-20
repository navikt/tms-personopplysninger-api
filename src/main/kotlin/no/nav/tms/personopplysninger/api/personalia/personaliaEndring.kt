package no.nav.tms.personopplysninger.api.personalia

import no.nav.tms.personopplysninger.api.personalia.pdl.PendingEndring

data class EndringResult(
    val statusType: String,
    val error: ErrorMessage?
) {
    companion object {
        fun withError(feilrespons: PendingEndring) = EndringResult (
            statusType = "ERROR",
            error = ErrorMessage(message = feilrespons.tpsBeskrivelse())
        )

        fun ok() = EndringResult(
            statusType = "OK",
            error = null,
        )
    }
}

data class ErrorMessage(
    val message: String? = null,
    val details: Map<String, List<String>>? = null
)
