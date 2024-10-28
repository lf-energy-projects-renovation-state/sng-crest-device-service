// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.service

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.FirmwareFactory
import org.gxf.crestdeviceservice.FirmwareFactory.getFirmwareEntity
import org.gxf.crestdeviceservice.FirmwareFactory.getFirmwares
import org.gxf.crestdeviceservice.FirmwareFactory.getPreviousFirmwareEntity
import org.gxf.crestdeviceservice.device.entity.Device
import org.gxf.crestdeviceservice.device.service.DeviceService
import org.gxf.crestdeviceservice.firmware.entity.FirmwareFactory
import org.gxf.crestdeviceservice.firmware.mapper.FirmwareMapper
import org.gxf.crestdeviceservice.firmware.repository.FirmwareRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class FirmwareServiceTest {
    private val firmwareRepository = mock<FirmwareRepository>()
    private val firmwarePacketRepository = mock<firmwarePacketRepository>()
    private val firmwareMapper = mock<FirmwareMapper>()
    private val firmwareHashService = mock<FirmwareHashService>()
    private val deviceService = mock<DeviceService>()
    private val firmwareService = FirmwareService(firmwareRepository, firmwareMapper)

    @Test
    fun processFirmware() {
        val firmwareFile = FirmwareFactory.getFirmwareFile()
        val firmwareEntity = getFirmwareEntity(firmwareFile.name)
        val previousFirmware = getPreviousFirmwareEntity()
        val allFirmwareEntities = listOf(previousFirmware, firmwareEntity)
        val firmwares = getFirmwares()
        whenever(firmwareMapper.mapFirmwareFileToEntity(firmwareFile)).thenReturn(firmwareEntity)
        whenever(firmwareRepository.findAll()).thenReturn(allFirmwareEntities)
        whenever(firmwareMapper.mapEntitiesToFirmwares(listOf(previousFirmware, firmwareEntity))).thenReturn(firmwares)

        val result = firmwareService.processFirmware(firmwareFile)

        verify(firmwareRepository).save(firmwareEntity)
        assertThat(result).isEqualTo(firmwares)
    }

    @Test
    fun shouldReturnFirmware() {
        val firmwareName = "useThisFirmware.txt"
        val expectedFirmware = FirmwareFactory.getFirmware()
        whenever(firmwareRepository.findByName(firmwareName)).thenReturn(expectedFirmware)

        val actualFirmware = firmwareService.findFirmwareByName(firmwareName)

        assertThat(actualFirmware).isSameAs(expectedFirmware)
    }

    @Test
    fun shouldReturnPacket() {
        val deviceId = "test-device"
        val deviceSecret = "super-duper-hush-hush"
        val device = Device("id", deviceSecret)
        val packetNumber = 0
        val firmware = FirmwareFactory.getFirmware()
        val packet = FirmwareFactory.getFirmwarePacket(firmware)

        whenever(firmwarePacketRepository.findByFirmwareAndPacketNumber(firmware, packetNumber)).thenReturn(packet)
        whenever(deviceService.getDevice(deviceId)).thenReturn(device)
        whenever(firmwareHashService.generateDeviceSpecificPacket(packet, deviceSecret)).thenReturn(packet.packet)

        val actualPacket = firmwareService.getPacketForDevice(firmware, packetNumber, deviceId)

        assertThat(actualPacket).isEqualTo(packet.packet)
    }
}
