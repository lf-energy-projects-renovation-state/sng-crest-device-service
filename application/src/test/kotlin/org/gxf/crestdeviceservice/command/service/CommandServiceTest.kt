package org.gxf.crestdeviceservice.command.service

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.repository.CommandRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.Optional
import java.util.UUID
import com.alliander.sng.Command as ExternalCommand

class CommandServiceTest {
    private val commandRepository = mock<CommandRepository>()
    private val commandService = CommandService(commandRepository)

    @Test
    fun `Check if command is allowed`() {
        whenever(commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any()))
            .thenReturn(testCommandEntity().copy(timestampIssued = Instant.now().minusSeconds(100)))

        val result = commandService.shouldBeRejected(testExternalCommand())
        assertThat(result).isEmpty
    }

    @Test
    fun `Check if command is rejected when command is unknown`() {
        whenever(commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any()))
            .thenReturn(testCommandEntity().copy(status = Command.CommandStatus.PENDING))

        val command =  testExternalCommand()
        command.command = "UNKNOWN"
        val result = commandService.shouldBeRejected(command)
        assertThat(result).isNotEmpty
    }

    @Test
    fun `Check if command is rejected when latest same command is in the future`() {
        whenever(commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any()))
            .thenReturn(testCommandEntity().copy(timestampIssued = Instant.now().plusSeconds(100)))

        val result = commandService.shouldBeRejected(testExternalCommand())
        assertThat(result).isNotEmpty
    }

    @Test
    fun `Check if existing pending same command is canceled it exists`() {
        val existingPendingCommand = testCommandEntity()
        val newCommand = testExternalCommand()
        whenever(commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any()))
            .thenReturn(existingPendingCommand)

        val commandToBeCanceled = commandService.existingCommandToBeCanceled(newCommand)

        assertThat(commandToBeCanceled).isEqualTo(Optional.of(existingPendingCommand))
    }

    @Test
    fun `Check if no command is canceled if no existing same pending command exists`() {
        val existingPendingCommand = testCommandEntity()
        val newCommand = testExternalCommand()
        whenever(commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any()))
            .thenReturn(null)

        val commandToBeCanceled = commandService.existingCommandToBeCanceled(newCommand)

        assertThat(commandToBeCanceled).isEmpty
    }

    private val deviceId = "device-id"

    private fun testExternalCommand() = ExternalCommand.newBuilder()
        .setDeviceId(deviceId)
        .setCorrelationId(UUID.randomUUID())
        .setTimestamp(Instant.now())
        .setCommand(Command.CommandType.REBOOT.name)
        .setValue(null)
        .build()

    private fun testCommandEntity() = Command(
        id = UUID.randomUUID(),
        deviceId = deviceId,
        correlationId = UUID.randomUUID(),
        timestampIssued = Instant.now(),
        type = Command.CommandType.REBOOT,
        commandValue = null,
        status = Command.CommandStatus.IN_PROGRESS
    )
}
