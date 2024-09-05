// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.consumer

import com.alliander.sng.CommandStatus
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.ExternalCommandFactory
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.mapper.CommandFeedbackMapper.externalCommandToCommandFeedback
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.psk.service.PskService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.refEq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CommandConsumerTest {
    private val commandService = mock<CommandService>()
    private val commandFeedbackService = mock<CommandFeedbackService>()
    private val pskService = mock<PskService>()
    private val commandConsumer =
        CommandConsumer(commandService, commandFeedbackService, pskService)

    private val externalCommand = ExternalCommandFactory.externalRebootCommand()

    @Test
    fun commandSaved() {
        whenever(commandService.validate(externalCommand)).thenReturn(null)
        whenever(commandService.existingCommandToBeCancelled(externalCommand)).thenReturn(null)

        commandConsumer.handleIncomingCommand(externalCommand)

        verify(commandService).saveExternalCommandAsPending(externalCommand)
    }

    @Test
    fun commandRejected() {
        val reason = "Because reasons"
        val commandFeedback = externalCommandToCommandFeedback(externalCommand, CommandStatus.Rejected, reason)
            whenever(commandService.validate(externalCommand)).thenReturn(reason)

        commandConsumer.handleIncomingCommand(externalCommand)

        verify(commandFeedbackService)
            .sendFeedback(refEq(commandFeedback, "timestampStatus"))
        verify(commandService, times(0))
            .existingCommandToBeCancelled(any<com.alliander.sng.Command>())
        verify(commandService, times(0))
            .saveExternalCommandAsPending(any<com.alliander.sng.Command>())
    }

    @Test
    fun existingCommandCancelled() {
        val existingPendingCommand = CommandFactory.pendingRebootCommand()

        whenever(commandService.validate(externalCommand)).thenReturn(null)
        whenever(commandService.existingCommandToBeCancelled(externalCommand))
            .thenReturn(existingPendingCommand)

        commandConsumer.handleIncomingCommand(externalCommand)

        verify(commandService)
            .saveCommandWithNewStatus(existingPendingCommand, Command.CommandStatus.CANCELLED)
        verify(commandService).saveExternalCommandAsPending(externalCommand)
    }
}
