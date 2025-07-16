// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.TestConstants.DEVICE_ID
import org.gxf.crestdeviceservice.model.Downlink
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class DeviceMessageServiceTest {
    @MockK(relaxed = true)
    private lateinit var messageProducerService: MessageProducerService

    @MockK(relaxed = true)
    private lateinit var downlinkService: DownlinkService

    @MockK(relaxed = true)
    private lateinit var payloadService: PayloadService

    @InjectMockKs private lateinit var deviceMessageService: DeviceMessageService

    private val downlink = Downlink()
    private val jsonNode = JsonNodeFactory.instance.objectNode()

    @BeforeEach
    fun setUp() {
        every { downlinkService.createDownlink() } returns downlink
    }

    @Test
    fun `should gather downlink and return normally when no errors occur`() {
        performAndAssert()
    }

    @Test
    fun `should gather downlink and return normally when an error occurs in payloadService`() {
        every { payloadService.processPayload(any(), any(), any()) } throws (RuntimeException("Oops"))

        performAndAssert()
    }

    @Test
    fun `should gather downlink and return normally when an error occurs in downlinkService`() {
        every { downlinkService.getDownlinkForDevice(any(), any()) } throws (RuntimeException("Oops"))

        performAndAssert()
    }

    private fun performAndAssert() {
        val resultingDownlink = deviceMessageService.processDeviceMessage(jsonNode, DEVICE_ID)

        // In all tested cases the downlink should be valid and 'SUCCESS'
        // (actual Downlink processing is not part of this test)
        assertThat(resultingDownlink).isEqualTo(Downlink.RESPONSE_SUCCESS)

        // Even if the downlinkService call fails, the payloadService call should be performed
        verify { payloadService.processPayload(DEVICE_ID, jsonNode, downlink) }

        // Even if the payloadService call fails, the downlinkService call should be performed
        verify { downlinkService.getDownlinkForDevice(DEVICE_ID, downlink) }
    }
}
