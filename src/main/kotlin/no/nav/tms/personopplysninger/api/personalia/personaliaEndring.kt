package no.nav.tms.personopplysninger.api.personalia

data class Endring(
    val status: Status = Status(),
    var statusType: String = "OK",
    var error: Error? = null
) {
    fun confirmedOk(): Boolean {
        return ("DONE" == status.statusType && !hasTpsError()) || "OK" == status.statusType
    }

    fun isPending(): Boolean {
        return "PENDING" == status.statusType
    }

    fun addValidationError() {
        this.statusType = "ERROR"
        this.error = Error(message = getTpsBeskrivelse())
    }

    fun hasTpsError(): Boolean {
        for (substatus in status.substatus) {
            if ("TPS".equals(substatus.domene, ignoreCase = true)) {
                return "ERROR" == substatus.status
            }
        }
        return false
    }

    private fun getTpsBeskrivelse(): String? {
        for (substatus in status.substatus) {
            if ("TPS".equals(substatus.domene, ignoreCase = true)) {
                return substatus.beskrivelse
            }
        }
        return null
    }

    class Status {
        val endringId: Int? = null
        val statusType: String? = null
        val substatus: List<Substatus> = emptyList()
    }

    class Substatus {
        val beskrivelse: String? = null
        val domene: String? = null
        val kode: String? = null
        val referanse: String? = null
        val status: String? = null
    }

    data class Error(
        val message: String? = null,
        val details: Map<String, List<String>>? = null
    )
}
