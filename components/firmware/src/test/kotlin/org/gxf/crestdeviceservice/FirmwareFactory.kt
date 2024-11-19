// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import java.util.UUID
import org.gxf.crestdeviceservice.FirmwareTestConstants.FIRMWARE_FROM_VERSION
import org.gxf.crestdeviceservice.FirmwareTestConstants.FIRMWARE_NAME
import org.gxf.crestdeviceservice.FirmwareTestConstants.FIRMWARE_PACKET_0
import org.gxf.crestdeviceservice.FirmwareTestConstants.FIRMWARE_UUID
import org.gxf.crestdeviceservice.FirmwareTestConstants.FIRMWARE_VERSION
import org.gxf.crestdeviceservice.FirmwareTestConstants.PREVIOUS_FIRMWARE_UUID
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket
import org.springframework.core.io.ClassPathResource
import org.springframework.mock.web.MockMultipartFile

object FirmwareFactory {

    fun getFirmwareFile(fileName: String): MockMultipartFile {
        val firmwareFile = ClassPathResource(fileName).file
        return MockMultipartFile("file", firmwareFile.name, "text/plain", firmwareFile.readBytes())
    }

    fun getFirmwareEntity(name: String): Firmware {
        val firmware = Firmware(FIRMWARE_UUID, name, FIRMWARE_VERSION, PREVIOUS_FIRMWARE_UUID, mutableListOf())
        val packet = getFirmwarePacket(firmware)
        firmware.packets.add(packet)
        return firmware
    }

    fun getFirmwareEntityWithoutPreviousVersion(name: String): Firmware {
        val firmware = Firmware(FIRMWARE_UUID, name, FIRMWARE_VERSION, null, mutableListOf())
        val packet = getFirmwarePacket(firmware)
        firmware.packets.add(packet)
        return firmware
    }

    fun getPreviousFirmwareEntity(): Firmware =
        Firmware(PREVIOUS_FIRMWARE_UUID, FIRMWARE_NAME, FIRMWARE_FROM_VERSION, UUID.randomUUID(), mutableListOf())

    private fun getFirmwarePacket(firmware: Firmware) = FirmwarePacket(firmware, 0, FIRMWARE_PACKET_0)
}
