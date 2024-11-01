// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.CommandFactory.pendingPskCommand
import org.gxf.crestdeviceservice.CommandFactory.pendingPskSetCommand
import org.gxf.crestdeviceservice.CommandFactory.pendingRebootCommand
import org.gxf.crestdeviceservice.CommandFactory.rebootCommandInProgress
import org.gxf.crestdeviceservice.TestConstants
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.generator.CommandGenerator
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.config.MessageProperties
import org.gxf.crestdeviceservice.model.Downlink
import org.gxf.crestdeviceservice.psk.service.PskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DownlinkServiceTest {
    private val pskService = mockk<PskService>()
    private val commandService = mockk<CommandService>()
    private val messageProperties = MessageProperties(1024)
    private val pskCommandGenerator = mockk<CommandGenerator>()
    private val pskSetCommandGenerator = mockk<CommandGenerator>()
    private lateinit var downlinkService: DownlinkService
    private val deviceId = TestConstants.DEVICE_ID

    private val pskCommandString = "this-is-the-generated-PSK-command"
    private val pskSetCommandString = "this-is-the-generated-PSK:SET-command"

    @BeforeEach
    fun setUp() {
        every { pskCommandGenerator.getSupportedCommand() } returns Command.CommandType.PSK
        every { pskCommandGenerator.generateCommandString(any()) } returns pskCommandString
        every { pskSetCommandGenerator.getSupportedCommand() } returns Command.CommandType.PSK_SET
        every { pskSetCommandGenerator.generateCommandString(any()) } returns pskSetCommandString

        downlinkService =
            DownlinkService(
                pskService,
                commandService,
                messageProperties,
                listOf(pskCommandGenerator, pskSetCommandGenerator)
            )
    }

    @Test
    fun shouldUseGeneratorWhenAvailableForCommand() {
        val pskCommandPending = pendingPskCommand()

        every { commandService.getAllPendingCommandsForDevice(deviceId) } returns listOf(pskCommandPending)
        every { pskService.readyForPskSetCommand(deviceId) } returns true
        every { commandService.saveCommand(any()) } answers { firstArg() }

        val downlink = downlinkService.getDownlinkForDevice(deviceId, Downlink())

        verify { pskCommandGenerator.generateCommandString(pskCommandPending) }

        assertThat(downlink).isEqualTo("!$pskCommandString")
    }

    @Test
    fun shouldUseCommandTypeDownlinkWhenGeneratorIsNotAvailable() {
        val rebootCommandPending = pendingRebootCommand()

        every { commandService.getAllPendingCommandsForDevice(deviceId) } returns listOf(rebootCommandPending)
        every { commandService.saveCommand(any()) } answers { firstArg() }

        val downlink = downlinkService.getDownlinkForDevice(deviceId, Downlink())

        verify(exactly = 0) { pskCommandGenerator.generateCommandString(rebootCommandPending) }

        assertThat(downlink).isEqualTo("!${Command.CommandType.REBOOT.downlink}")
    }

    @Test
    fun shouldReturnPskDownlinkWhenThereIsANewPsk() {
        val pskCommand = pendingPskCommand()
        val pskSetCommand = pendingPskSetCommand()
        val pskCommandsPending = listOf(pskCommand, pskSetCommand)

        every { commandService.getAllPendingCommandsForDevice(deviceId) } returns pskCommandsPending
        every { pskService.readyForPskSetCommand(deviceId) } returns true
        every { pskService.setPskToPendingForDevice(deviceId) } returns mockk()
        every { commandService.saveCommand(pskCommand) } returns pskCommand
        every { commandService.saveCommand(pskSetCommand) } returns pskSetCommand

        val result = downlinkService.getDownlinkForDevice(deviceId, Downlink())

        assertThat(result).isEqualTo("!$pskCommandString;$pskSetCommandString")
        assertThat(pskCommand.status).isEqualTo(Command.CommandStatus.IN_PROGRESS)
        assertThat(pskSetCommand.status).isEqualTo(Command.CommandStatus.IN_PROGRESS)
    }

    @Test
    fun shouldSendPendingCommandsIfNoCommandInProgress() {
        val rebootCommand = pendingRebootCommand()
        val pendingCommands = listOf(rebootCommand)

        every { pskService.readyForPskSetCommand(deviceId) } returns false
        every { commandService.getAllPendingCommandsForDevice(deviceId) } returns pendingCommands
        every { commandService.getFirstCommandInProgressForDevice(deviceId) } returns null
        every { commandService.saveCommand(rebootCommand) } returns rebootCommand

        val result = downlinkService.getDownlinkForDevice(deviceId, Downlink())

        val expectedDownlink = "!CMD:REBOOT"
        assertThat(result).isEqualTo(expectedDownlink)
    }

    @Test
    fun shouldReturnNoActionDownlinkWhenThereIsNoNewPskAndACommandIsInProgress() {
        every { pskService.readyForPskSetCommand(deviceId) } returns false
        every { commandService.getAllPendingCommandsForDevice(deviceId) } returns listOf()
        every { commandService.getFirstCommandInProgressForDevice(deviceId) } returns rebootCommandInProgress()

        val result = downlinkService.getDownlinkForDevice(deviceId, Downlink())

        assertThat(result).isEqualTo("0")
    }

    @Test
    fun shouldReturnNoActionDownlinkWhenThereIsNoNewPskOrPendingCommand() {
        every { commandService.getAllPendingCommandsForDevice(deviceId) } returns listOf()
        every { pskService.readyForPskSetCommand(deviceId) } returns false

        val result = downlinkService.getDownlinkForDevice(deviceId, Downlink())

        assertThat(result).isEqualTo("0")
    }
}
