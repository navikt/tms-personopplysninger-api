package no.nav.tms.personopplysninger.api.personalia

data class EndringResult(
    val statusType: String,
    val error: ErrorMessage?
) {
    companion object {
        fun withError(pendingEndring: PendingEndring) = EndringResult (
            statusType = "ERROR",
            error = ErrorMessage(message = pendingEndring.tpsBeskrivelse())
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

data class PendingEndring(
    val status: Status = Status(),
) {
    fun confirmedOk(): Boolean {
        return ("DONE" == status.statusType && !hasTpsError()) || "OK" == status.statusType
    }

    fun isPending(): Boolean {
        return "PENDING" == status.statusType
    }

    fun hasTpsError(): Boolean {
        for (substatus in status.substatus) {
            if (substatus.erTpsDomene()) {
                return "ERROR" == substatus.status
            }
        }
        return false
    }

    fun tpsBeskrivelse(): String? {
        for (substatus in status.substatus) {
            if (substatus.erTpsDomene()) {
                return substatus.beskrivelse
            }
        }
        return null
    }

    data class Status(
        val endringId: Int? = null,
        val statusType: String? = null,
        val substatus: List<Substatus> = emptyList()
    )

    data class Substatus(
        val beskrivelse: String? = null,
        val domene: String? = null,
        val kode: String? = null,
        val referanse: String? = null,
        val status: String? = null
    ) {
        fun erTpsDomene() = domene?.uppercase() == "TPS"
    }
}
