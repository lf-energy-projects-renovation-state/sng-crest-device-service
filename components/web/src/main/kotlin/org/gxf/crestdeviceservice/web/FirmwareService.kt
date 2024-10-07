// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.web

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.http.HttpClient
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FirmwareService(val httpClient: HttpClient) {
    private val logger = KotlinLogging.logger {}

    fun processFirmwareFile(file: MultipartFile): Int {
        logger.info { "Processing file: ${file.originalFilename}" }
        val firmwareFileDTO = mapFirmwareFileToDTO(file)
        logger.info { "Processing ${firmwareFileDTO.packets.size} packets from firmware file" }

        httpClient.postFirmware(firmwareFileDTO)

        return firmwareFileDTO.packets.size
    }

    fun mapFirmwareFileToDTO(file: MultipartFile): FirmwareDTO {
        val fileContent = String(file.inputStream.readBytes())

        logger.debug { "Contents of firmware file:\n${fileContent}" }

        val packets = fileContent.lines()

        return FirmwareDTO(file.originalFilename!!, packets)
    }
}
