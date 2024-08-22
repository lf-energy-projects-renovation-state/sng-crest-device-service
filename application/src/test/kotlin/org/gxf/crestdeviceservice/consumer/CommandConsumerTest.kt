package org.gxf.crestdeviceservice.consumer

import com.alliander.sng.CommandStatus
import org.gxf.crestdeviceservice.TestHelper
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.mockito.kotlin.mock

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class CommandConsumerTest {
    private val commandService = mock<CommandService>()
    private val commandFeedbackService = mock<CommandFeedbackService>()
    private val commandConsumer = CommandConsumer(commandService, commandFeedbackService)

    private val externalCommand = TestHelper.receivedRebootCommand()

    @Test
    fun commandRejected() {
        whenever(commandService.shouldBeRejected(externalCommand))
            .thenReturn(Optional.of("rejected"))

        commandConsumer.handleIncomingCommand(externalCommand)

        verify(commandFeedbackService).sendFeedback(eq(externalCommand), eq(CommandStatus.Rejected), any<String>())
        verify(commandService, times(0)).existingCommandToBeCanceled(any<com.alliander.sng.Command>())
        verify(commandService, times(0)).saveExternalCommandAsPending(any<com.alliander.sng.Command>())
    }

    @Test
    fun existingCommandCanceled() {
        val existingPendingCommand = TestHelper.pendingRebootCommand()
        val existingCommandCanceled = existingPendingCommand.copy(status = Command.CommandStatus.CANCELED)

        whenever(commandService.shouldBeRejected(externalCommand)).thenReturn(Optional.empty())
        whenever(commandService.existingCommandToBeCanceled(externalCommand))
            .thenReturn(Optional.of(existingPendingCommand))

        commandConsumer.handleIncomingCommand(externalCommand)

        verify(commandService).saveCommandEntity(existingCommandCanceled)
        verify(commandService).saveExternalCommandAsPending(externalCommand)
    }

    @Test
    fun noExistingSameCommand() {
        whenever(commandService.shouldBeRejected(externalCommand)).thenReturn(Optional.empty())
        whenever(commandService.existingCommandToBeCanceled(externalCommand)).thenReturn(Optional.empty())

        commandConsumer.handleIncomingCommand(externalCommand)

        verify(commandService).saveExternalCommandAsPending(externalCommand)
    }
}