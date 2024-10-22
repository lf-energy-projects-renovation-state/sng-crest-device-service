// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.web

import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.io.ClassPathResource
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(FirmwareWebController::class)
class FirmwareWebControllerTest {
    @Autowired private lateinit var mockMvc: MockMvc

    @MockBean private lateinit var firmwareWebService: FirmwareWebService

    @Test
    fun handleFileUpload() {
        val fileName = "RTU#FULL#TO#23.10.txt"
        val firmwareFile = ClassPathResource(fileName).file
        val multipartFile = MockMultipartFile("file", firmwareFile.name, "text/plain", firmwareFile.readBytes())

        mockMvc
            .perform(multipart("https://localhost:9001/web/firmware").file(multipartFile))
            .andExpect(status().isFound)

        verify(firmwareWebService).processFirmwareFile(multipartFile)
    }
}
