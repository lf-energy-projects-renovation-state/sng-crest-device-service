// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.consumer

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.ExternalCommandFactory
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.exception.CommandValidationException
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.psk.service.PskService
import org.junit.jupiter.api.Test

class CommandConsumerTest {
    private val commandService = mockk<CommandService>()
    private val commandFeedbackService = mockk<CommandFeedbackService>()
    private val pskService = mockk<PskService>()
    private val commandConsumer = CommandConsumer(commandService, commandFeedbackService, pskService)

    private val externalCommand = ExternalCommandFactory.externalRebootCommand()
    private val command = CommandFactory.pendingRebootCommand()

    @Test
    fun rebootCommandSaved() {
        every { commandService.isPskCommand(any()) } returns false
        justRun { commandService.validate(any()) }
        justRun { commandFeedbackService.sendReceivedFeedback(any()) }
        justRun { commandService.cancelOlderCommandIfNecessary(any()) }
        every { commandService.saveCommand(any()) } answers { firstArg() }

        commandConsumer.handleIncomingCommand(externalCommand)

        verify { commandService.validate(match(::contentEquals)) }
        verify { commandFeedbackService.sendReceivedFeedback(match(::contentEquals)) }
        verify { commandService.cancelOlderCommandIfNecessary(match(::contentEquals)) }
        verify { commandService.saveCommand(match(::contentEquals)) }
    }

    @Test
    fun pskCommandSavedAndKeyGenerated() {
        every { commandService.isPskCommand(any()) } returns true
        justRun { commandService.validate(any()) }
        justRun { commandFeedbackService.sendReceivedFeedback(any()) }
        justRun { commandService.cancelOlderCommandIfNecessary(any()) }
        justRun { pskService.generateNewReadyKeyForDevice(any()) }
        every { commandService.saveCommand(any()) } answers { firstArg() }

        commandConsumer.handleIncomingCommand(externalCommand)

        verify { commandService.validate(match(::contentEquals)) }
        verify { commandFeedbackService.sendReceivedFeedback(match(::contentEquals)) }
        verify { commandService.cancelOlderCommandIfNecessary(match(::contentEquals)) }
        verify { pskService.generateNewReadyKeyForDevice(command.deviceId) }
        verify { commandService.saveCommand(match(::contentEquals)) }
    }

    @Test
    fun `Check if command is rejected when command is unknown`() {
        justRun { commandFeedbackService.sendRejectionFeedback(any(), any()) }

        val command = ExternalCommandFactory.externalRebootCommand().apply { command = "UNKNOWN" }

        commandConsumer.handleIncomingCommand(command)

        verify { commandFeedbackService.sendRejectionFeedback("Command unknown: UNKNOWN", command) }
    }

    @Test
    fun commandRejectedForOtherReasons() {
        justRun { commandFeedbackService.sendRejectionFeedback(any(), any()) }

        val reason = "There is a newer command of the same type"

        every { commandService.validate(any()) } throws CommandValidationException(reason)

        commandConsumer.handleIncomingCommand(externalCommand)

        verify { commandFeedbackService.sendRejectionFeedback(reason, externalCommand) }
        verify(exactly = 0) { commandFeedbackService.sendReceivedFeedback(any()) }
        verify(exactly = 0) { commandService.saveCommand(any<Command>()) }
    }

    private fun contentEquals(command: Command) =
        (command.deviceId == this.command.deviceId) &&
            (command.correlationId == this.command.correlationId) &&
            (command.type == this.command.type) &&
            (command.commandValue == this.command.commandValue) &&
            (command.status == this.command.status)
}
