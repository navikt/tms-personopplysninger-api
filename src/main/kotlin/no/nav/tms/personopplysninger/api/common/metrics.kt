package no.nav.tms.personopplysninger.api.common

import io.prometheus.metrics.core.metrics.Counter
import io.prometheus.metrics.core.metrics.Gauge
import io.prometheus.metrics.core.metrics.Histogram
import io.prometheus.metrics.model.registry.PrometheusRegistry
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue

class ConsumerMetrics private constructor(
    private val consumerName: String
) {
    companion object {

        private val registry = PrometheusRegistry.defaultRegistry

        fun init(caller: () -> Unit): ConsumerMetrics {
            val name = caller.javaClass.name
            val slicedName =
                when {
                    name.contains("Kt$") -> name.substringBefore("Kt$")
                    name.contains("$") -> name.substringBefore("$")
                    else -> name
                }
            return ConsumerMetrics(slicedName)
        }

        private val consumerResponseTimeHistogram = Histogram.builder()
            .name("consumer_response_time_histogram")
            .help("Hvor lenge consumer må vente på svar i sekunder")
            .labelNames("consumer", "request")
            .register(registry)

        private val consumerResponseTimeSum = Gauge.builder()
            .name("consumer_response_time_sum")
            .help("Total tid consumer må vente på svar i sekunder")
            .labelNames("consumer", "request")
            .register(registry)

        private val consumerCallsCounter = Counter.builder()
            .name("consumer_calls_counter")
            .help("Antall kall fra consumer")
            .labelNames("consumer", "request")
            .register(registry)

        private val consumerErrorsCounter = Counter.builder()
            .name("consumer_errors_counter")
            .help("Antall kall fra consumer som feiler")
            .labelNames("consumer", "request")
            .register(registry)
    }

    suspend fun <T> measureRequest(requestName: String, block: suspend () -> T): T = try {
        measureTimedValue {
            block()
        }.let { (result, duration) ->
            consumerCallsCounter.labelValues(consumerName, requestName).inc()
            consumerResponseTimeHistogram.labelValues(consumerName, requestName).observe(duration.toDouble(DurationUnit.SECONDS))
            consumerResponseTimeSum.labelValues(consumerName, requestName).inc(duration.toDouble(DurationUnit.SECONDS))

            result
        }
    } catch (e: Exception) {
        consumerErrorsCounter.labelValues(consumerName, requestName).inc()

        throw e
    }
}
