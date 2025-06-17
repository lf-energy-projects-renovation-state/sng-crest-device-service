// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.mapper

import com.alliander.sng.FirmwareType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gxf.crestdeviceservice.FirmwareFactory.getFirmwareEntity
import org.gxf.crestdeviceservice.FirmwareFactory.getFirmwareEntityWithoutPreviousVersion
import org.gxf.crestdeviceservice.FirmwareFactory.getFirmwareFile
import org.gxf.crestdeviceservice.FirmwareTestConstants.FIRMWARE_FILE_DELTA_NAME
import org.gxf.crestdeviceservice.FirmwareTestConstants.FIRMWARE_FILE_DELTA_PACKET_SIZE
import org.gxf.crestdeviceservice.FirmwareTestConstants.FIRMWARE_FILE_DELTA_VERSION
import org.gxf.crestdeviceservice.FirmwareTestConstants.FIRMWARE_NAME
import org.gxf.crestdeviceservice.FirmwareTestConstants.FIRMWARE_VERSION
import org.gxf.crestdeviceservice.FirmwareTestConstants.FIRMWARE_WRONG_NAME
import org.gxf.crestdeviceservice.firmware.exception.FirmwareException
import org.junit.jupiter.api.Test
import org.springframework.web.multipart.MultipartFile

class FirmwareMapperTest {

    private val firmwareMapper: FirmwareMapper = FirmwareMapper()

    @Test
    fun shouldMapFirmwareFileToEntity() {
        // arrange
        val file: MultipartFile = getFirmwareFile(FIRMWARE_FILE_DELTA_NAME)

        // act
        val firmware = firmwareMapper.mapFirmwareFileToEntity(file)

        // assert
        assertThat(firmware.name).isEqualTo(FIRMWARE_FILE_DELTA_NAME)
        assertThat(firmware.version).isEqualTo(FIRMWARE_FILE_DELTA_VERSION)
        assertThat(firmware.packets.size).isEqualTo(FIRMWARE_FILE_DELTA_PACKET_SIZE)
    }

    @Test
    fun shouldMapEntitiesToFirmwares() {
        val firmwareEntities = listOf(getFirmwareEntity(FIRMWARE_NAME))

        val result = firmwareMapper.mapEntitiesToFirmwares(firmwareEntities)

        val firmware = result.firmwares.first()
        assertThat(firmware.name).isEqualTo(FIRMWARE_NAME)
        assertThat(firmware.version).isEqualTo(FIRMWARE_VERSION)
        assertThat(firmware.type).isEqualTo(FirmwareType.device)
        assertThat(firmware.fromVersion).isNull()
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
