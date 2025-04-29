// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class DeviceMessageService(
    private val messageProducerService: MessageProducerService,
    private val downlinkService: DownlinkService,
    private val payloadService: PayloadService,
) {
    companion object {
        val logger = KotlinLogging.logger {}
    }

    private val locks: MutableMap<String, Any> = mutableMapOf()

    fun processDeviceMessage(message: JsonNode, deviceId: String): String {
        messageProducerService.produceMessage(message)

        synchronized(lock(deviceId)) {
            val downlink = downlinkService.createDownlink()

            runCatching { payloadService.processPayload(deviceId, message, downlink) }
                .onFailure { exception ->
                    logger.error(exception) {
                        "Error while processing payload for device $deviceId. Continuing processing the downlink anyway"
                    }
                }

            runCatching { downlinkService.getDownlinkForDevice(deviceId, downlink) }
                .onFailure { exception ->
                    logger.error(exception) {
                        "Error while getting downlink for device $deviceId. Continuing processing the downlink anyway"
                    }
                }

            return downlink.getDownlink()
        }
    }

    @Synchronized
    private fun lock(substationIdentification: String) = locks.computeIfAbsent(substationIdentification) { _ -> Any() }
}
