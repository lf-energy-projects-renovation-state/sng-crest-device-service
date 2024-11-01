// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.TestHelper
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.service.FirmwareService
import org.gxf.crestdeviceservice.model.DeviceMessage
import org.gxf.crestdeviceservice.model.Downlink
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PayloadServiceTest {
    @MockK private lateinit var urcService: UrcService
    @MockK private lateinit var firmwareService: FirmwareService
    @MockK private lateinit var commandService: CommandService
    @MockK(relaxed = true) private lateinit var commandFeedbackService: CommandFeedbackService

    @InjectMockKs private lateinit var payloadService: PayloadService

    private val mapper = ObjectMapper()

    @Test
    fun shouldProcessUrcs() {
        val message = TestHelper.messageTemplate()
        val downlink = Downlink()
        val deviceId = "device-id"

        justRun { urcService.interpretUrcsInMessage(any(), any()) }

        payloadService.processPayload(deviceId, message, downlink)

        verify { urcService.interpretUrcsInMessage(deviceId, message) }
    }

    @Test
    fun shouldSupplyOtaCommandForFmc() {
        val packetNumber = 3
        val packetCount = 20
        val message = TestHelper.messageTemplate()
        message.set<JsonNode>(DeviceMessage.FMC_FIELD, mapper.readTree(packetNumber.toString()))
        val downlink = Downlink()
        val deviceId = "device-id"
        val firmwareCommand = CommandFactory.firmwareCommandInProgress()
        val firmwareName = firmwareCommand.commandValue!!
        val firmware = Firmware(UUID.randomUUID(), firmwareName, "some-hash", null)
        val otaCommand = "OTA0003ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        justRun { urcService.interpretUrcsInMessage(any(), any()) }
        every { commandService.getAllCommandsInProgressForDevice(deviceId) } returns listOf(firmwareCommand)
        every { firmwareService.findFirmwareByName(firmwareName) } returns firmware
        every { firmwareService.getPacketForDevice(firmware, packetNumber, deviceId) } returns otaCommand
        every { firmwareService.countFirmwarePacketsByName(firmwareName) } returns packetCount

        payloadService.processPayload(deviceId, message, downlink)

        verify { urcService.interpretUrcsInMessage(deviceId, message) }
        assertThat(downlink.getDownlink()).contains(otaCommand)

        verify { commandFeedbackService.sendProgressFeedback(packetNumber + 1, packetCount, firmwareCommand) }
    }
}
