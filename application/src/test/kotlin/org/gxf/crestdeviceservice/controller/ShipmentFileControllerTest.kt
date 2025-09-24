// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonMappingException
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.gxf.crestdeviceservice.service.ShipmentFileService
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.stream.Stream

@WebMvcTest(ShipmentFileController::class, excludeAutoConfiguration = [SecurityAutoConfiguration::class])
class ShipmentFileControllerTest {
    @MockkBean
    private lateinit var shipmentFileService: ShipmentFileService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @ParameterizedTest
    @MethodSource("thrownExceptions")
    @Throws(Exception::class)
    fun testUploadShipmentFile_InvalidJson_ShouldReturnBadRequest(exceptionToBeThrown: Exception) {
        val invalidJson = """{ "shipmentId": 123, """ // Malformed JSON

        every { shipmentFileService.processShipmentFile(any()) } throws exceptionToBeThrown

        mockMvc //
            .perform(
                multipart("https://localhost:9001/web/shipmentfile").file(
                    MockMultipartFile(
                        "file",
                        "invalid.json",
                        "application/json",
                        invalidJson.toByteArray(),
                    ),
                ),
            )
            .andExpect(status().is3xxRedirection)
    }

    companion object {
        private val jsonParser: JsonParser? = null

        @JvmStatic
        fun thrownExceptions(): Stream<Exception> = Stream.of(
            JsonParseException("Parsing error"),
            JsonMappingException.from(jsonParser, "Mapping error"),
            RuntimeException("Something else"),
        )
    }
}
