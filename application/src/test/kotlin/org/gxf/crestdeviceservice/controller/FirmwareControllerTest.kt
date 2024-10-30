// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import java.util.UUID
import org.gxf.crestdeviceservice.FirmwareFileFactory
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket
import org.gxf.crestdeviceservice.firmware.service.FirmwareService
import org.gxf.crestdeviceservice.service.FirmwareProducerService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(FirmwareController::class)
class FirmwareControllerTest {
    @Autowired private lateinit var mockMvc: MockMvc

    @MockBean private lateinit var firmwareService: FirmwareService

    @MockBean private lateinit var firmwareProducerService: FirmwareProducerService

    @Test
    fun shouldProcessFirmwareAndSendAllFirmwares() {
        val firmwareFile = FirmwareFileFactory.getFirmwareFile()
        val firmwareEntity = Firmware(UUID.randomUUID(), "test-fw", "1.23", packets = mutableListOf())
        firmwareEntity.packets += FirmwarePacket(firmwareEntity, 0, "some content")

        whenever(firmwareService.processFirmware(firmwareFile)).thenReturn(firmwareEntity)

        mockMvc.perform(multipart("https://localhost:9001/web/firmware").file(firmwareFile)).andExpect(status().isFound)

        verify(firmwareService).processFirmware(firmwareFile)
        verify(firmwareProducerService).sendAllFirmwares()
    }
}
