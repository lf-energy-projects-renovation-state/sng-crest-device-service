// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.consumer

import com.alliander.sng.CommandStatus
import org.gxf.crestdeviceservice.TestHelper
import org.gxf.crestdeviceservice.command.consumer.CommandConsumer
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.psk.service.PskService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CommandConsumerTest {
    private val commandService = mock<CommandService>()
    private val commandFeedbackService = mock<CommandFeedbackService>()
    private val pskService = mock<PskService>()
    private val commandConsumer =
        CommandConsumer(commandService, commandFeedbackService, pskService)

    private val externalCommand = TestHelper.receivedRebootCommand()

    @Test
    fun commandSaved() {
        whenever(commandService.reasonForRejection(externalCommand)).thenReturn(null)
        whenever(commandService.existingCommandToBeCanceled(externalCommand)).thenReturn(null)

        commandConsumer.handleIncomingCommand(externalCommand)

        verify(commandService).saveExternalCommandAsPending(externalCommand)
    }

    @Test
    fun commandRejected() {
        whenever(commandService.reasonForRejection(externalCommand)).thenReturn("rejected")

        commandConsumer.handleIncomingCommand(externalCommand)

        verify(commandFeedbackService)
            .sendFeedback(eq(externalCommand), eq(CommandStatus.Rejected), any<String>())
        verify(commandService, times(0))
            .existingCommandToBeCanceled(any<com.alliander.sng.Command>())
        verify(commandService, times(0))
            .saveExternalCommandAsPending(any<com.alliander.sng.Command>())
    }

    @Test
    fun existingCommandCanceled() {
        val existingPendingCommand = TestHelper.pendingRebootCommand()

        whenever(commandService.reasonForRejection(externalCommand)).thenReturn(null)
        whenever(commandService.existingCommandToBeCanceled(externalCommand))
            .thenReturn(existingPendingCommand)

        commandConsumer.handleIncomingCommand(externalCommand)

        verify(commandService)
            .saveCommandWithNewStatus(existingPendingCommand, Command.CommandStatus.CANCELED)
        verify(commandService).saveExternalCommandAsPending(externalCommand)
    }
}
