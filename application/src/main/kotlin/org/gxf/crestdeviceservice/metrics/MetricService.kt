package org.gxf.crestdeviceservice.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service

@Service
class MetricService(meterRegistry: MeterRegistry) {

    companion object {
        const val METRIC_PREFIX = "deviceservice"
    }

    private val pskInvalidCounter = Counter
            .builder("$METRIC_PREFIX.psk.incorrect")
            .description("Counts the number of times a device tried to connect with a wrong/unknown PSK")
            .register(meterRegistry)

    fun incrementPskInvalidCounter() = pskInvalidCounter.increment()
}