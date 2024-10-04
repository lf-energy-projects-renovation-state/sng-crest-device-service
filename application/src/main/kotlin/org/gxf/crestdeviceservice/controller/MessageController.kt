// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.service.DownlinkService
import org.gxf.crestdeviceservice.service.MessageProducerService
import org.gxf.crestdeviceservice.service.UrcService
import org.springframework.http.ResponseEntity
import org.springframework.lang.NonNull
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/sng")
class MessageController(
    private val messageProducerService: MessageProducerService,
    private val downlinkService: DownlinkService,
    private val urcService: UrcService
) {
    private val logger = KotlinLogging.logger {}

    private val locks: MutableMap<String, Any> = mutableMapOf()

    /**
     * This endpoint handles incoming crest device messages. Responses are generated synchronously to avoid sending the
     * same downlink twice.
     */
    @PostMapping("/{identity}")
    fun post(@NonNull @PathVariable identity: String, @NonNull @RequestBody body: JsonNode): ResponseEntity<String> {

        logger.debug { "Processing message $body" }
        messageProducerService.produceMessage(body)
        logger.debug { "Processed message" }

        synchronized(lock(identity)) {
            try {
                urcService.interpretURCsInMessage(identity, body)
                val downlink = downlinkService.getDownlinkForDevice(identity, body)
                return ResponseEntity.ok(downlink)
            } catch (e: Exception) {
                logger.error(e) {
                    "Exception occurred while interpreting message from or creating downlink for device $identity"
                }
                return ResponseEntity.internalServerError().build()
            }
        }
    }

    @Synchronized
    private fun lock(substationIdentification: String) = locks.computeIfAbsent(substationIdentification) { _ -> Any() }
}
