// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.mapper

import java.util.UUID
import org.gxf.crestdeviceservice.firmware.dto.FirmwareDTO
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket

object FirmwareMapper {
    fun mapFirmwareDTOToEntity(firmwareDTO: FirmwareDTO): Firmware {
        val firmware =
            Firmware(
                UUID.randomUUID(),
                firmwareDTO.name,
                getFirmwareVersionFromName(firmwareDTO.name),
                getPreviousFirmwareFromName(firmwareDTO.name),
                mutableListOf())

        val packets = mapLinesToPackets(firmwareDTO.packets, firmware)

        firmware.packets.addAll(packets)

        return firmware
    }

    // todo private this and other functions
    fun getFirmwareVersionFromName(name: String) =
        name.substringAfter("#TO#").substringBefore(".txt")

    private fun getPreviousFirmwareFromName(name: String): UUID {
        return UUID.randomUUID() // todo
    }

    private fun mapLinesToPackets(dtoPackets: List<String>, firmware: Firmware) =
        dtoPackets.map { line -> mapLineToPacket(line, firmware) }

    private fun mapLineToPacket(line: String, firmware: Firmware) =
        FirmwarePacket(firmware, getPacketNumberFromLine(line), line)

    fun getPacketNumberFromLine(line: String) =
        line.substringAfter("OTA").substring(0, 4).toInt() // todo NumberFormatException
}
