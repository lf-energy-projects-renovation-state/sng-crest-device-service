// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.TestHelper.pendingRebootCommand
import org.gxf.crestdeviceservice.TestHelper.rebootCommandInProgress
import org.gxf.crestdeviceservice.TestHelper.receivedRebootCommand
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.repository.CommandRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CommandServiceTest {
    private val commandRepository = mock<CommandRepository>()
    private val commandService = CommandService(commandRepository)

    @Test
    fun `Check if command is allowed`() {
        whenever(
                commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(
                    any(), any()))
            .thenReturn(
                pendingRebootCommand().copy(timestampIssued = Instant.now().minusSeconds(100)))

        val result = commandService.reasonForRejection(receivedRebootCommand())
        assertThat(result).isNull()
    }

    @Test
    fun `Check if command is rejected when command is unknown`() {
        whenever(
                commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(
                    any(), any()))
            .thenReturn(pendingRebootCommand().copy(status = Command.CommandStatus.PENDING))

        val command = receivedRebootCommand()
        command.command = "UNKNOWN"
        val result = commandService.reasonForRejection(command)
        assertThat(result).isNotNull()
    }

    @Test
    fun `Check if command is rejected when latest same command is in the future`() {
        whenever(
                commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(
                    any(), any()))
            .thenReturn(
                pendingRebootCommand().copy(timestampIssued = Instant.now().plusSeconds(100)))

        val result = commandService.reasonForRejection(receivedRebootCommand())
        assertThat(result).isNotNull()
    }

    @Test
    fun `Check if command is rejected when  same command is in progress`() {
        whenever(
                commandRepository.findAllByDeviceIdAndTypeAndStatusOrderByTimestampIssuedAsc(
                    any(), any(), any()))
            .thenReturn(listOf(rebootCommandInProgress()))

        val result = commandService.reasonForRejection(receivedRebootCommand())
        assertThat(result).isNotNull()
    }

    @Test
    fun `Check if existing pending same command is canceled if it exists`() {
        val existingPendingCommand = pendingRebootCommand()
        val newCommand = receivedRebootCommand()
        whenever(
                commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(
                    any(), any()))
            .thenReturn(existingPendingCommand)

        val commandToBeCanceled = commandService.existingCommandToBeCanceled(newCommand)

        assertThat(commandToBeCanceled).isEqualTo(existingPendingCommand)
    }

    @Test
    fun `Check if no command is canceled if no existing same pending command exists`() {
        val newCommand = receivedRebootCommand()
        whenever(
                commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(
                    any(), any()))
            .thenReturn(null)

        val commandToBeCanceled = commandService.existingCommandToBeCanceled(newCommand)

        assertThat(commandToBeCanceled).isNull()
    }
}
