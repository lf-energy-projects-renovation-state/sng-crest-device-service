package org.gxf.crestdeviceservice

import org.gxf.crestdeviceservice.metrics.MetricService
import org.gxf.crestdeviceservice.psk.PskController
import org.gxf.crestdeviceservice.psk.PskService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(PskController::class)
class DeviceCredentialsControllerTest {

    @Autowired
    private lateinit var mvcRequest: MockMvc

    @MockBean
    private lateinit var pskService: PskService

    @MockBean
    private lateinit var metricService: MetricService

    @Test
    fun shouldReturn404WhenPskForIdentityIsNotFound() {
        `when`(
            pskService.getCurrentActiveKey(
                any<String>()
            )
        ).thenReturn(null)

        mvcRequest.perform(
                MockMvcRequestBuilders
                        .get("/psk")
                        .header("x-device-identity", "identity"))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun shouldReturnKeyForIdentity() {
        `when`(
            pskService.getCurrentActiveKey(
                any<String>()
            )
        ).thenReturn("key")

        mvcRequest.perform(
                MockMvcRequestBuilders
                        .get("/psk")
                        .header("x-device-identity", "identity"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().string("key"))
    }
}
