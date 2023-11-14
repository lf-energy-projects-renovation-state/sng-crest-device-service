package org.gxf.crestdeviceservice

import org.gxf.crestdeviceservice.psk.PskController
import org.gxf.crestdeviceservice.psk.PskService
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(PskController::class)
class PskControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var mock: PskService

    @Test
    fun shouldReturn404WhenPskForIdentityIsNotFound() {
        Mockito.`when`(mock.getCurrentPsk(any())).thenReturn(null)

        mockMvc.perform(
                MockMvcRequestBuilders.get("/psk")
                        .header("x-device-identity", "identity"))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun shouldReturnKeyForIdentity() {
        Mockito.`when`(mock.getCurrentPsk(any())).thenReturn("key")

        mockMvc.perform(
                MockMvcRequestBuilders.get("/psk")
                        .header("x-device-identity", "identity"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().string("key"))
    }
}