// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import com.alliander.sng.FirmwareType
import com.alliander.sng.Firmwares
import java.util.UUID
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_FROM_VERSION
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_NAME
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_PACKET_0
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_UUID
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_VERSION
import org.gxf.crestdeviceservice.TestConstants.PREVIOUS_FIRMWARE_UUID
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket
import org.springframework.core.io.ClassPathResource
import org.springframework.mock.web.MockMultipartFile

object FirmwareFactory {
    fun getFirmwareFile(): MockMultipartFile {
        val fileName = "RTU#FULL#TO#23.10.txt"
        val firmwareFile = ClassPathResource(fileName).file
        return MockMultipartFile("file", firmwareFile.name, "text/plain", firmwareFile.readBytes())
    }

    fun getFirmwareEntity(name: String): Firmware {
        val firmware = Firmware(FIRMWARE_UUID, name, FIRMWARE_VERSION, PREVIOUS_FIRMWARE_UUID, mutableListOf())
        val packet = getFirmwarePacket(firmware)
        firmware.packets.add(packet)
        return firmware
    }

    fun getPreviousFirmwareEntity(): Firmware =
        Firmware(PREVIOUS_FIRMWARE_UUID, FIRMWARE_NAME, FIRMWARE_FROM_VERSION, UUID.randomUUID(), mutableListOf())

    fun getFirmwarePacket(firmware: Firmware) = FirmwarePacket(firmware, 0, FIRMWARE_PACKET_0)

    fun getFirmwares() = Firmwares.newBuilder().setFirmwares(listOf(firmware(), previousFirmware())).build()

    private fun firmware() =
        com.alliander.sng.Firmware.newBuilder()
            .setName(FIRMWARE_NAME)
            .setType(FirmwareType.device)
            .setVersion(FIRMWARE_VERSION)
            .setFromVersion(FIRMWARE_FROM_VERSION)
            .setNumberOfPackages(2)
            .build()

    private fun previousFirmware() =
        com.alliander.sng.Firmware.newBuilder()
            .setName(FIRMWARE_NAME)
            .setType(FirmwareType.device)
            .setVersion(FIRMWARE_FROM_VERSION)
            .setFromVersion(FIRMWARE_FROM_VERSION)
            .setNumberOfPackages(2)
            .build()
}
