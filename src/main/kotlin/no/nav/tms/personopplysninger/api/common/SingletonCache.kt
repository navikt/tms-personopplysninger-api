package no.nav.tms.personopplysninger.api.common

import java.time.Duration
import java.time.Instant

class SingletonCache<T>(
    private val expireAfter: Duration
) {
    private lateinit var lastUpdated: Instant
    private var value: T? = null

    suspend fun get(fetcher: suspend () -> T): T {
        if (value == null || isExpired()) {
            value = fetcher()
            lastUpdated = Instant.now()
        }

        return value!!
    }

    private fun isExpired(): Boolean {
        return lastUpdated + expireAfter < Instant.now()
    }
}
