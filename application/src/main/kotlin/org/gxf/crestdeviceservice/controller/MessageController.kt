// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.service.DeviceMessageService
import org.springframework.http.ResponseEntity
import org.springframework.lang.NonNull
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sng")
class MessageController(private val deviceMessageService: DeviceMessageService) {
    private val logger = KotlinLogging.logger {}

    /**
     * This endpoint handles incoming crest device messages. Responses are generated synchronously to avoid sending the
     * same downlink twice.
     */
    @PostMapping("/{identity}")
    fun post(@NonNull @PathVariable identity: String, @NonNull @RequestBody body: JsonNode): ResponseEntity<String> {
        logger.debug { "Processing message $body for device $identity" }

        return try {
            val downlink = deviceMessageService.processDeviceMessage(body, identity)
            logger.debug { "Sending downlink '$downlink' to device $identity" }
            ResponseEntity.ok(downlink)
        } catch (e: Exception) {
            logger.error(e) {
                "Exception occurred while interpreting message from or creating downlink for device $identity"
            }
            ResponseEntity.internalServerError().build()
        } finally {
            logger.debug { "Processed message" }
        }
    }
}
