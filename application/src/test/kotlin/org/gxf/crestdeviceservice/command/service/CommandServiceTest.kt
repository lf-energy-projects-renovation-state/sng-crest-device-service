package org.gxf.crestdeviceservice.command.service

import com.alliander.sng.CommandStatus
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.TestHelper
import org.gxf.crestdeviceservice.TestHelper.pendingRebootCommand
import org.gxf.crestdeviceservice.TestHelper.receivedRebootCommand
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.repository.CommandRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.Optional

class CommandServiceTest {
    private val commandRepository = mock<CommandRepository>()
    private val commandFeedbackService = mock<CommandFeedbackService>()
    private val commandService = CommandService(commandRepository, commandFeedbackService)

    private val deviceId = "TST-01"

    @Test
    fun `Check if command is allowed`() {
        whenever(commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any()))
            .thenReturn(pendingRebootCommand().copy(timestampIssued = Instant.now().minusSeconds(100)))

        val result = commandService.shouldBeRejected(receivedRebootCommand())
        assertThat(result).isEmpty
    }

    @Test
    fun `Check if command is rejected when command is unknown`() {
        whenever(commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any()))
            .thenReturn(pendingRebootCommand().copy(status = Command.CommandStatus.PENDING))

        val command =  receivedRebootCommand()
        command.command = "UNKNOWN"
        val result = commandService.shouldBeRejected(command)
        assertThat(result).isNotEmpty
    }

    @Test
    fun `Check if command is rejected when latest same command is in the future`() {
        whenever(commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any()))
            .thenReturn(pendingRebootCommand().copy(timestampIssued = Instant.now().plusSeconds(100)))

        val result = commandService.shouldBeRejected(receivedRebootCommand())
        assertThat(result).isNotEmpty
    }

    @Test
    fun `Check if existing pending same command is canceled it exists`() {
        val existingPendingCommand = pendingRebootCommand()
        val newCommand = receivedRebootCommand()
        whenever(commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any()))
            .thenReturn(existingPendingCommand)

        val commandToBeCanceled = commandService.existingCommandToBeCanceled(newCommand)

        assertThat(commandToBeCanceled).isEqualTo(Optional.of(existingPendingCommand))
    }

    @Test
    fun `Check if no command is canceled if no existing same pending command exists`() {
        val newCommand = receivedRebootCommand()
        whenever(commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any()))
            .thenReturn(null)

        val commandToBeCanceled = commandService.existingCommandToBeCanceled(newCommand)

        assertThat(commandToBeCanceled).isEmpty
    }

    @Test
    fun handleCommandError() {
        val commandInProgress = TestHelper.rebootCommandInProgress()
        val commandError = commandInProgress.copy(status = Command.CommandStatus.ERROR)
        whenever(commandRepository.save(commandError)).thenReturn(commandError)

        commandService.handleCommandError(deviceId, commandInProgress, listOf("ERR, DLER"))

        verify(commandRepository).save(commandError)
        verify(commandFeedbackService).sendFeedback(eq(commandError), eq(CommandStatus.Error), any<String>())
    }

    @Test
    fun handleCommandUrcsReboot() {
        val commandInProgress = TestHelper.rebootCommandInProgress()
        val commandSuccessful = commandInProgress.copy(status = Command.CommandStatus.SUCCESSFUL)
        whenever(commandRepository.save(commandSuccessful)).thenReturn(commandSuccessful)

        commandService.handleCommandUrcs(deviceId, commandInProgress, listOf("INIT", "WDR"))

        verify(commandRepository).save(commandSuccessful)
        verify(commandFeedbackService).sendFeedback(eq(commandSuccessful), eq(CommandStatus.Successful), any<String>())
    }
}
