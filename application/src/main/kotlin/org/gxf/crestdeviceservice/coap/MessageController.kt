// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.coap

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.lang.NonNull
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/sng")
class MessageController(private val messageService: MessageService, private val downlinkService: DownlinkService) {

    private val logger = KotlinLogging.logger {}

    @PostMapping("/{identity}")
    fun post(@NonNull @PathVariable identity: String, @NonNull @RequestBody body: JsonNode): ResponseEntity<String> {

        logger.debug { "Processing message $body" }
        messageService.handleMessage(body)
        logger.debug { "Processed message" }

        return ResponseEntity.ok(downlinkService.getDownlinkForIdentity(identity))
    }
}
