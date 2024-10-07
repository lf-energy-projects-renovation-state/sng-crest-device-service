// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.mapper

import java.util.UUID
import org.gxf.crestdeviceservice.firmware.dto.FirmwareDTO
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket
import org.gxf.crestdeviceservice.firmware.exception.FirmwareException
import org.gxf.crestdeviceservice.firmware.repository.FirmwareRepository
import org.springframework.stereotype.Service

@Service
class FirmwareMapper(private val firmwareRepository: FirmwareRepository) {
    fun mapFirmwareDTOToEntity(firmwareDTO: FirmwareDTO): Firmware {
        val firmware =
            Firmware(
                UUID.randomUUID(),
                firmwareDTO.name,
                getFirmwareVersionFromName(firmwareDTO.name),
                getPreviousFirmwareIdFromName(firmwareDTO.name),
                mutableListOf())

        val packets = mapLinesToPackets(firmwareDTO.packets, firmware)

        firmware.packets.addAll(packets)

        return firmware
    }

    private fun getFirmwareVersionFromName(name: String) =
        name.substringAfter("#TO#").substringBefore(".txt")

    private fun getPreviousFirmwareIdFromName(name: String): UUID? {
        if (!name.contains("#FROM#")) {
            return null
        }
        val previousFirmwareVersion = name.substringAfter("#FROM#").substringBefore("#TO#")
        val previousFirmware = firmwareRepository.findByVersion(previousFirmwareVersion)
        if (previousFirmware != null) {
            return previousFirmware.id
        } else {
            throw FirmwareException(
                "Previous firmware with version $previousFirmwareVersion does not exist")
        }
    }

    private fun mapLinesToPackets(dtoPackets: List<String>, firmware: Firmware) =
        dtoPackets.mapIndexed { index, line -> mapLineToPacket(index, line, firmware) }

    private fun mapLineToPacket(index: Int, line: String, firmware: Firmware) =
        FirmwarePacket(firmware, index, line)
}
