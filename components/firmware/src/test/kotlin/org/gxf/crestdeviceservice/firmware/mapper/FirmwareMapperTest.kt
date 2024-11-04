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
import org.gxf.crestdeviceservice.FirmwareFactory.getFirmwareEntity
import org.gxf.crestdeviceservice.FirmwareFactory.getPreviousFirmwareEntity
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_FROM_VERSION
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_NAME
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_VERSION
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_WRONG_NAME
import org.gxf.crestdeviceservice.TestConstants.PREVIOUS_FIRMWARE_UUID
import org.gxf.crestdeviceservice.firmware.exception.FirmwareException
import org.gxf.crestdeviceservice.firmware.repository.FirmwareRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class FirmwareMapperTest {
    @MockK private lateinit var firmwareRepository: FirmwareRepository

    @InjectMockKs private lateinit var firmwareMapper: FirmwareMapper

    @Test
    fun mapEntitiesToFirmwares() {
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
    fun mapEntitiesToFirmwaresThrowsException() {
        val firmwareEntities = listOf(getFirmwareEntity(FIRMWARE_WRONG_NAME))

        assertThatThrownBy { firmwareMapper.mapEntitiesToFirmwares(firmwareEntities) }
            .isInstanceOf(FirmwareException::class.java)
    }
}
