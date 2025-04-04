// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.mapper

import com.alliander.sng.Firmware as ExternalFirmware
import com.alliander.sng.FirmwareType
import com.alliander.sng.Firmwares
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket
import org.gxf.crestdeviceservice.firmware.exception.FirmwareException
import org.gxf.crestdeviceservice.firmware.repository.FirmwareRepository
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class FirmwareMapper(private val firmwareRepository: FirmwareRepository) {
    private val logger = KotlinLogging.logger {}

    fun mapFirmwareFileToEntity(file: MultipartFile): Firmware {
        val fileContent = String(file.inputStream.readBytes())

        val name = checkNotNull(file.originalFilename) { "File name should not be null" }

        val firmware =
            Firmware(
                UUID.randomUUID(),
                name,
                getFirmwareVersionFromName(name),
                getPreviousFirmwareIdFromName(name),
                mutableListOf(),
            )

        val packets = mapLinesToPackets(fileContent.lines(), firmware)

        firmware.packets.addAll(packets)

        return firmware
    }

    private fun getFirmwareVersionFromName(name: String) = name.substringAfter("#TO#").substringBefore(".txt")

    private fun getPreviousFirmwareIdFromName(name: String): UUID? {
        val previousVersionRegex = """(?<=#FROM#)(.*)(?=#TO#)""".toRegex()
        return previousVersionRegex.find(name)?.let {
            val previousFirmwareVersion = it.value
            val previousFirmware = firmwareRepository.findByVersion(previousFirmwareVersion)
            if (previousFirmware == null) {
                logger.warn { "Previous firmware with version $previousFirmwareVersion does not exist" }
            }
            previousFirmware?.id
        }
    }

    private fun mapLinesToPackets(dtoPackets: List<String>, firmware: Firmware) =
        dtoPackets.mapIndexed { index, line -> mapLineToPacket(index, line, firmware) }

    private fun mapLineToPacket(index: Int, line: String, firmware: Firmware) = FirmwarePacket(firmware, index, line)

    fun mapEntitiesToFirmwares(firmwareEntities: List<Firmware>): Firmwares {
        val firmwares = firmwareEntities.map { firmware -> mapEntityToSchema(firmware) }
        return Firmwares.newBuilder().setFirmwares(firmwares).build()
    }

    private fun mapEntityToSchema(firmware: Firmware): ExternalFirmware =
        ExternalFirmware.newBuilder()
            .setName(firmware.name)
            .setType(getFirmwareTypeFromName(firmware.name))
            .setVersion(firmware.version)
            .setFromVersion(getFromVersion(firmware))
            .setNumberOfPackages(firmware.packets.size)
            .build()

    private fun getFirmwareTypeFromName(name: String): FirmwareType {
        val type = name.substringBefore("#")
        return translateType(type)
    }

    private fun translateType(type: String): FirmwareType =
        when (type) {
            "RTU" -> FirmwareType.device
            "MODEM" -> FirmwareType.modem
            else -> {
                throw FirmwareException("Firmware type $type does not exist")
            }
        }

    private fun getFromVersion(firmware: Firmware): String? {
        return firmware.previousFirmwareId?.let {
            val previousFirmware = firmwareRepository.findById(firmware.previousFirmwareId)
            previousFirmware.get().version
        }
    }
}
