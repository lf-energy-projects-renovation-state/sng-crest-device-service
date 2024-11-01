// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.TestConstants.DEVICE_ID
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(RspController::class)
class RspControllerTest {
    @MockkBean private lateinit var commandService: CommandService

    @Autowired private lateinit var mockMvc: MockMvc

    @Test
    fun `should save RSP command`() {
        every { commandService.saveCommand(any()) } answers { firstArg() }

        mockMvc //
            .perform(post("/test/{deviceId}/rsp", DEVICE_ID)) //
            .andExpect(status().isOk)

        val commandSlot = slot<Command>()

        verify { commandService.saveCommand(capture(commandSlot)) }

        val command = commandSlot.captured
        assertThat(command.deviceId).isEqualTo(DEVICE_ID)
        assertThat(command.status).isEqualTo(Command.CommandStatus.PENDING)
        assertThat(command.type).isEqualTo(Command.CommandType.RSP)
    }

    @Test
    fun `should save RSP2 command`() {
        every { commandService.saveCommand(any()) } answers { firstArg() }

        mockMvc //
            .perform(post("/test/{deviceId}/rsp2", DEVICE_ID)) //
            .andExpect(status().isOk)

        val commandSlot = slot<Command>()

        verify { commandService.saveCommand(capture(commandSlot)) }

        val command = commandSlot.captured
        assertThat(command.deviceId).isEqualTo(DEVICE_ID)
        assertThat(command.status).isEqualTo(Command.CommandStatus.PENDING)
        assertThat(command.type).isEqualTo(Command.CommandType.RSP2)
    }
}
