// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.controller

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import org.gxf.crestdeviceservice.service.MessageService
import org.springframework.http.ResponseEntity
import org.springframework.lang.NonNull
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("\${crest-device-service.http.endpoint}")
class CoapMessageController(private val messageService: MessageService) {

    private val logger = KotlinLogging.logger {}

    @PostMapping("/{id}")
    fun post(@NonNull @PathVariable id: String, @NonNull @RequestBody body: JsonNode): ResponseEntity<String> {
        logger.debug { "Processing message $body" }
        messageService.handleMessage(body)
        logger.debug { "Processed message" }
        return ResponseEntity.ok("0")
    }
}
