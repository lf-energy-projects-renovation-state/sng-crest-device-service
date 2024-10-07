// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.firmware.dto.FirmwareDTO
import org.gxf.crestdeviceservice.firmware.exception.FirmwareException
import org.gxf.crestdeviceservice.firmware.service.FirmwareService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.lang.NonNull
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/firmware")
class FirmwareController(val firmwareService: FirmwareService) {
    private val logger = KotlinLogging.logger {}

    @PostMapping("/{name}")
    fun post(
        @NonNull @PathVariable name: String,
        @NonNull @RequestBody firmware: FirmwareDTO
    ): ResponseEntity<String> {
        try {
            logger.debug { "Processing firmware with name $name" }
            firmwareService.processFirmware(firmware)
            logger.debug { "Processed firmware" }
            return ResponseEntity.ok("Firmware successfully processed")
        } catch (e: FirmwareException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }
}
