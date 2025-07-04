// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.alliander.sng.DeviceCredentials
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.model.ShipmentFileDTO
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ShipmentFileService(private val deviceCredentialsService: DeviceCredentialsService) {
    private val logger = KotlinLogging.logger {}
    private val mapper = jacksonObjectMapper()

    fun processShipmentFile(file: MultipartFile): Int {
        val shipmentFileDTO = mapShipmentFileToDTO(file)
        logger.info { "Processing ${shipmentFileDTO.devices.size} devices from shipment file" }
        val credentials = mapDTOToDeviceCredentials(shipmentFileDTO)

        credentials.forEach { credential -> deviceCredentialsService.importDeviceCredentials(credential) }
        return shipmentFileDTO.devices.size
    }

    fun mapShipmentFileToDTO(file: MultipartFile): ShipmentFileDTO {
        val fileContent = String(file.inputStream.readBytes())

        logger.debug { "Contents of shipment file:\n${fileContent}" }

        return mapper.readValue<ShipmentFileDTO>(fileContent)
    }

    fun mapDTOToDeviceCredentials(shipmentFile: ShipmentFileDTO): List<DeviceCredentials> {
        return shipmentFile.devices.map {
            with(it.rtu) {
                DeviceCredentials.newBuilder()
                    .setImei(rtuId)
                    .setPsk(pskEncrypted)
                    .setSecret(pskChangeSecretEncrypted)
                    .setKeyRef(pskEncryptionKeyRef)
                    .build()
            }
        }
    }
}
