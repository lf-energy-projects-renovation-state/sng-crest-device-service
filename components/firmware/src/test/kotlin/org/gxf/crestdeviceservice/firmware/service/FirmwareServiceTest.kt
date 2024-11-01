// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.FirmwareFactory
import org.gxf.crestdeviceservice.FirmwareFactory.getFirmwareEntity
import org.gxf.crestdeviceservice.device.entity.Device
import org.gxf.crestdeviceservice.device.service.DeviceService
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket
import org.gxf.crestdeviceservice.firmware.mapper.FirmwareMapper
import org.gxf.crestdeviceservice.firmware.repository.FirmwarePacketRepository
import org.gxf.crestdeviceservice.firmware.repository.FirmwareRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class FirmwareServiceTest {
    @MockK private lateinit var firmwareRepository: FirmwareRepository
    @MockK private lateinit var firmwarePacketRepository: FirmwarePacketRepository
    @MockK private lateinit var firmwareMapper: FirmwareMapper
    @MockK private lateinit var firmwareHashService: FirmwareHashService
    @MockK private lateinit var deviceService: DeviceService

    @InjectMockKs private lateinit var firmwareService: FirmwareService

    @Test
    fun processFirmware() {
        val firmwareFile = FirmwareFactory.getFirmwareFile()
        val firmwareEntity = getFirmwareEntity(firmwareFile.name)

        every { firmwareMapper.mapFirmwareFileToEntity(firmwareFile) } returns firmwareEntity
        every { firmwareRepository.findByName(any()) } returns null
        every { firmwareRepository.save(firmwareEntity) } returns firmwareEntity
        every { firmwareRepository.save(any()) } answers { firstArg() }

        val result = firmwareService.processFirmware(firmwareFile)

        verify { firmwareRepository.save(firmwareEntity) }

        assertThat(result).isEqualTo(firmwareEntity)
    }

    @Test
    fun shouldReturnFirmware() {
        val firmwareName = "useThisFirmware.txt"
        val expectedFirmware = Firmware(UUID.randomUUID(), name = "a firmware", version = "3")

        every { firmwareRepository.findByName(firmwareName) } returns expectedFirmware

        val actualFirmware = firmwareService.findFirmwareByName(firmwareName)

        assertThat(actualFirmware).isSameAs(expectedFirmware)
    }

    @Test
    fun shouldCountPackets() {
        val firmwareName = "firmware"
        val packetCount = 10

        val firmware = Firmware(id = UUID.randomUUID(), name = firmwareName, version = "1", packets = mutableListOf())

        repeat(packetCount) { packetNumber ->
            firmware.packets += FirmwarePacket(firmware, packetNumber, "packet $packetNumber")
        }

        every { firmwareRepository.findByName(firmwareName) } returns firmware

        assertThat(firmwareService.countFirmwarePacketsByName(firmwareName)).isEqualTo(packetCount)
    }

    @Test
    fun shouldReturnPacket() {
        val deviceId = "test-device"
        val deviceSecret = "super-duper-hush-hush"
        val device = Device("id", deviceSecret)
        val packetNumber = 0
        val firmware = Firmware(UUID.randomUUID(), name = "a firmware", version = "3")
        val packet = FirmwarePacket(firmware, 0, "the-packet-contents")

        every { firmwarePacketRepository.findByFirmwareAndPacketNumber(firmware, packetNumber) } returns packet
        every { deviceService.getDevice(deviceId) } returns device
        every { firmwareHashService.generateDeviceSpecificPacket(packet, deviceSecret) } returns packet.packet

        val actualPacket = firmwareService.getPacketForDevice(firmware, packetNumber, deviceId)

        assertThat(actualPacket).isEqualTo(packet.packet)
    }
}
