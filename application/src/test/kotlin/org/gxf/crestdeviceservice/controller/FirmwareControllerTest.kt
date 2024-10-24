// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.gxf.crestdeviceservice.FirmwareFileFactory
import org.gxf.crestdeviceservice.FirmwaresFactory.getFirmwares
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

    private val objectMapper = ObjectMapper()

    @Test
    fun shouldProcessFirmwareAndSendAllFirmwares() {
        val firmwareFile = FirmwareFileFactory.getFirmwareFile()
        val firmwares = getFirmwares()

        whenever(firmwareService.processFirmware(firmwareFile)).thenReturn(firmwares)

        mockMvc.perform(multipart("https://localhost:9001/web/firmware").file(firmwareFile)).andExpect(status().isOk)

        verify(firmwareService).processFirmware(firmwareFile)
        verify(firmwareProducerService).send(firmwares)
    }
}
