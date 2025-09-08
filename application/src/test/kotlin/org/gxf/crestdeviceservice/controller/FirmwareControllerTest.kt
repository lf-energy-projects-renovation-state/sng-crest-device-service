// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import org.gxf.crestdeviceservice.FirmwareFileFactory
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket
import org.gxf.crestdeviceservice.firmware.service.FirmwareService
import org.gxf.crestdeviceservice.service.FirmwareProducerService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(FirmwareController::class, excludeAutoConfiguration = [SecurityAutoConfiguration::class])
class FirmwareControllerTest {
    @MockkBean private lateinit var firmwareService: FirmwareService

    @MockkBean private lateinit var firmwareProducerService: FirmwareProducerService

    @Autowired private lateinit var mockMvc: MockMvc

    @Test
    fun shouldProcessFirmwareAndSendAllFirmwares() {
        val firmwareFile = FirmwareFileFactory.getFirmwareFile()
        val firmwareEntity = Firmware(UUID.randomUUID(), "test-fw", "1.23", packets = mutableListOf())
        firmwareEntity.packets += FirmwarePacket(firmwareEntity, 0, "some content")

        every { firmwareService.processFirmware(firmwareFile) } returns firmwareEntity
        justRun { firmwareProducerService.sendAllFirmwares() }

        mockMvc //
            .perform(multipart("https://localhost:9001/web/firmware").file(firmwareFile)) //
            .andExpect(status().isFound)

        verify { firmwareService.processFirmware(firmwareFile) }
        verify { firmwareProducerService.sendAllFirmwares() }
    }
}
