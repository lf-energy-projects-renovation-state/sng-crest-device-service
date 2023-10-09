// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.adapter

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import org.gxf.crestdeviceservice.port.MessagePort
import org.springframework.http.ResponseEntity
import org.springframework.lang.NonNull
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/coap")
class CoapMessageAdapter(private val messagePort: MessagePort<JsonNode>) {
    companion object {
        val logger = KotlinLogging.logger {}
    }

    @PostMapping("/{id}")
    fun post(@NonNull @PathVariable id: String, @NonNull @RequestBody body: JsonNode): ResponseEntity<String> {
        logger.debug("Processing message {}", body)
        messagePort.handleMessage(body)
        logger.debug("Processed message")
        return ResponseEntity.ok("0")
    }
}