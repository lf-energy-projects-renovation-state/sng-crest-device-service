// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.http

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.web.FirmwareWebDTO
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.toEntity

@Component
class HttpClient(private val webClient: RestClient) {
    companion object {
        const val MESSAGE_PATH = "/firmware"
    }

    private val logger = KotlinLogging.logger {}

    @Throws(HttpClientErrorException::class, HttpServerErrorException::class)
    fun postFirmware(firmware: FirmwareWebDTO): ResponseEntity<String> {
        logger.debug {
            "Posting firmware with name ${firmware.name} and ${firmware.packets.size} packets"
        }

        try {
            val response = executeRequest(firmware)
            logger.debug {
                "Posted message with name ${firmware.name}, resulting response: $response"
            }
            return response
        } catch (e: Exception) {
            logger.warn(e) { "Error received while posting message with name ${firmware.name}" }
            throw e
        }
    }

    @Throws(HttpClientErrorException::class, HttpServerErrorException::class)
    private fun executeRequest(firmware: FirmwareWebDTO): ResponseEntity<String> =
        webClient
            .post()
            .uri("$MESSAGE_PATH/${firmware.name}")
            .body(firmware)
            .retrieve()
            .toEntity<String>()
}
