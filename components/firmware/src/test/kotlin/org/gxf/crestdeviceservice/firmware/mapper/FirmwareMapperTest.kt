// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.mapper

import com.alliander.sng.FirmwareType
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import java.util.Optional
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gxf.crestdeviceservice.firmware.FirmwareFactory.getFirmwareEntity
import org.gxf.crestdeviceservice.firmware.FirmwareFactory.getFirmwareEntityWithoutPreviousVersion
import org.gxf.crestdeviceservice.firmware.FirmwareFactory.getFirmwareFile
import org.gxf.crestdeviceservice.firmware.FirmwareFactory.getPreviousFirmwareEntity
import org.gxf.crestdeviceservice.firmware.FirmwareTestConstants.FIRMWARE_FILE_DELTA_NAME
import org.gxf.crestdeviceservice.firmware.FirmwareTestConstants.FIRMWARE_FILE_DELTA_PACKET_SIZE
import org.gxf.crestdeviceservice.firmware.FirmwareTestConstants.FIRMWARE_FILE_DELTA_PREVIOUS_VERSION
import org.gxf.crestdeviceservice.firmware.FirmwareTestConstants.FIRMWARE_FILE_DELTA_VERSION
import org.gxf.crestdeviceservice.firmware.FirmwareTestConstants.FIRMWARE_FROM_VERSION
import org.gxf.crestdeviceservice.firmware.FirmwareTestConstants.FIRMWARE_NAME
import org.gxf.crestdeviceservice.firmware.FirmwareTestConstants.FIRMWARE_VERSION
import org.gxf.crestdeviceservice.firmware.FirmwareTestConstants.FIRMWARE_WRONG_NAME
import org.gxf.crestdeviceservice.firmware.FirmwareTestConstants.PREVIOUS_FIRMWARE_UUID
import org.gxf.crestdeviceservice.firmware.exception.FirmwareException
import org.gxf.crestdeviceservice.firmware.repository.FirmwareRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.multipart.MultipartFile

@ExtendWith(MockKExtension::class)
class FirmwareMapperTest {
    @MockK private lateinit var firmwareRepository: FirmwareRepository

    @InjectMockKs private lateinit var firmwareMapper: FirmwareMapper

    @Test
    fun shouldMapFirmwareFileToEntity() {
        // arrange
        val file: MultipartFile = getFirmwareFile(FIRMWARE_FILE_DELTA_NAME)
        val previousFirmware = getPreviousFirmwareEntity()
        every { firmwareRepository.findByVersion(FIRMWARE_FILE_DELTA_PREVIOUS_VERSION) } returns previousFirmware

        // act
        val firmware = firmwareMapper.mapFirmwareFileToEntity(file)

        // assert
        assertThat(firmware.name).isEqualTo(FIRMWARE_FILE_DELTA_NAME)
        assertThat(firmware.version).isEqualTo(FIRMWARE_FILE_DELTA_VERSION)
        assertThat(firmware.previousFirmwareId).isEqualTo(previousFirmware.id)
        assertThat(firmware.packets.size).isEqualTo(FIRMWARE_FILE_DELTA_PACKET_SIZE)
    }

    @Test
    fun shouldMapFirmwareFileToEntityWhenNoPrevousVersionPresent() {
        // arrange
        val file: MultipartFile = getFirmwareFile(FIRMWARE_FILE_DELTA_NAME)
        every { firmwareRepository.findByVersion(FIRMWARE_FILE_DELTA_PREVIOUS_VERSION) } returns null

        // act
        val firmware = firmwareMapper.mapFirmwareFileToEntity(file)

        // assert
        assertThat(firmware.name).isEqualTo(FIRMWARE_FILE_DELTA_NAME)
        assertThat(firmware.version).isEqualTo(FIRMWARE_FILE_DELTA_VERSION)
        assertThat(firmware.previousFirmwareId).isNull()
        assertThat(firmware.packets.size).isEqualTo(FIRMWARE_FILE_DELTA_PACKET_SIZE)
    }

    @Test
    fun shouldMapEntitiesToFirmwares() {
        val firmwareEntities = listOf(getFirmwareEntity(FIRMWARE_NAME))
        val previousFirmware = getPreviousFirmwareEntity()

        every { firmwareRepository.findById(PREVIOUS_FIRMWARE_UUID) } returns Optional.of(previousFirmware)

        val result = firmwareMapper.mapEntitiesToFirmwares(firmwareEntities)

        val firmware = result.firmwares.first()
        assertThat(firmware.name).isEqualTo(FIRMWARE_NAME)
        assertThat(firmware.version).isEqualTo(FIRMWARE_VERSION)
        assertThat(firmware.type).isEqualTo(FirmwareType.device)
        assertThat(firmware.fromVersion).isEqualTo(FIRMWARE_FROM_VERSION)
        assertThat(firmware.numberOfPackages).isEqualTo(1)
    }

    @Test
    fun shouldMapEntitiesToFirmwaresWhenNoPreviousFirmwarePresent() {
        val firmwareEntities = listOf(getFirmwareEntityWithoutPreviousVersion(FIRMWARE_NAME))

        val result = firmwareMapper.mapEntitiesToFirmwares(firmwareEntities)

        val firmware = result.firmwares.first()
        assertThat(firmware.name).isEqualTo(FIRMWARE_NAME)
        assertThat(firmware.version).isEqualTo(FIRMWARE_VERSION)
        assertThat(firmware.type).isEqualTo(FirmwareType.device)
        assertThat(firmware.fromVersion).isNull()
        assertThat(firmware.numberOfPackages).isEqualTo(1)
    }

    @Test
    fun shouldThrowExceptionWhenUsingWrongFirmwareName() {
        val firmwareEntities = listOf(getFirmwareEntity(FIRMWARE_WRONG_NAME))

        assertThatThrownBy { firmwareMapper.mapEntitiesToFirmwares(firmwareEntities) }
            .isInstanceOf(FirmwareException::class.java)
    }
}
