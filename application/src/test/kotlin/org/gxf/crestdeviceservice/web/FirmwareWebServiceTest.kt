// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.web

import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.http.HttpClient
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.mock.web.MockMultipartFile

class FirmwareWebServiceTest {
    private val httpClient = mock<HttpClient>()
    private val firmwareWebService = FirmwareWebService(httpClient)

    @Test
    fun processFirmwareFile() {
        val firmwareFile = File("src/test/resources/RTU#FULL#TO#23.10.txt")
        val multipartFile = MockMultipartFile(firmwareFile.name, firmwareFile.name, null, firmwareFile.readBytes())
        val expectedSize = 13
        val firmwareFileDto = FirmwareWebMapper.mapFirmwareFileToDTO(multipartFile)

        val packetSize = firmwareWebService.processFirmwareFile(multipartFile)

        verify(httpClient).postFirmware(firmwareFileDto)
        assertThat(packetSize).isEqualTo(expectedSize)
    }
}
