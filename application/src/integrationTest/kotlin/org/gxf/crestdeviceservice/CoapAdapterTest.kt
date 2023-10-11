// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice

import org.gxf.crestdeviceservice.controller.CoapMessageController
import org.gxf.crestdeviceservice.service.MessageService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(CoapMessageController::class)
class CoapAdapterTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var mock: MessageService

    @Test
    fun shouldReturn0WhenNoConfigurationIsAvailable() {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/coap/{id}", 1)
                        .contentType("application/json").content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().string("0")
                )
    }
}