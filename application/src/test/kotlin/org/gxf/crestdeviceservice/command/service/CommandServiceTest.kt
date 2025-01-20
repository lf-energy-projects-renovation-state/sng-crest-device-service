// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import java.time.Instant
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gxf.crestdeviceservice.CommandFactory.pendingAnalogAlarmThresholdsCommand
import org.gxf.crestdeviceservice.CommandFactory.pendingRebootCommand
import org.gxf.crestdeviceservice.CommandFactory.rebootCommandInProgress
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.exception.CommandValidationException
import org.gxf.crestdeviceservice.command.repository.CommandRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@ExtendWith(MockKExtension::class)
class CommandServiceTest {
    @MockK private lateinit var commandRepository: CommandRepository
    @MockK(relaxed = true) private lateinit var commandFeedbackService: CommandFeedbackService

    @InjectMockKs private lateinit var commandService: CommandService

    @Test
    fun validateSucceeded() {
        every { //
            commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any())
        } returns pendingRebootCommand(Instant.now().minusSeconds(100))
        every {
            commandRepository.findAllByDeviceIdAndTypeAndStatusOrderByTimestampIssuedAsc(any(), any(), any())
        } returns listOf()

        assertThatNoException().isThrownBy { commandService.validate(pendingRebootCommand()) }
    }

    @Test
    fun `Check if command is rejected when latest same command is in the future`() {
        every { //
            commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any())
        } returns pendingRebootCommand(Instant.now().plusSeconds(100))

        assertThatThrownBy { commandService.validate(pendingRebootCommand()) }
            .usingRecursiveComparison()
            .isEqualTo(CommandValidationException("There is a newer command of the same type"))
    }

    @Test
    fun `Check if command is rejected when same command is in progress`() {
        every {
            commandRepository.findAllByDeviceIdAndTypeAndStatusOrderByTimestampIssuedAsc(any(), any(), any())
        } returns listOf(rebootCommandInProgress())

        assertThatThrownBy { commandService.validate(pendingRebootCommand()) }
            .usingRecursiveComparison()
            .isEqualTo(CommandValidationException("A command of the same type is already in progress."))
    }

    @ParameterizedTest
    @MethodSource("invalidCommandValues")
    fun `Check if command is rejected when analog alarm thresholds command does not match regex`(commandValue: String) {
        every { //
            commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any())
        } returns pendingRebootCommand(Instant.now().minusSeconds(100))
        every {
            commandRepository.findAllByDeviceIdAndTypeAndStatusOrderByTimestampIssuedAsc(any(), any(), any())
        } returns listOf()

        assertThatThrownBy { commandService.validate(pendingAnalogAlarmThresholdsCommand(value = commandValue)) }
            .usingRecursiveComparison()
            .isEqualTo(CommandValidationException("Analog alarm thresholds command value does not match regex."))
    }

    @Test
    fun `Check if existing pending same command is cancelled if it exists`() {
        val newCommand = pendingRebootCommand()
        val existingPendingCommand =
            pendingRebootCommand(timestampIssued = Instant.now().minusSeconds(100), correlationId = UUID.randomUUID())

        every { //
            commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any())
        } returns existingPendingCommand
        every { commandRepository.save(any()) } answers { firstArg() }

        commandService.cancelOlderCommandIfNecessary(newCommand)

        verify { commandFeedbackService.sendCancellationFeedback(existingPendingCommand, any()) }
        verify { commandRepository.save(existingPendingCommand) }

        assertThat(existingPendingCommand.status).isEqualTo(Command.CommandStatus.CANCELLED)
    }

    @Test
    fun `Check if no command is cancelled if no existing same pending command exists`() {
        val newCommand = pendingRebootCommand()

        every { commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(any(), any()) } returns null

        commandService.cancelOlderCommandIfNecessary(newCommand)

        verify(exactly = 0) { commandFeedbackService.sendCancellationFeedback(any(), any()) }
    }

    companion object {
        @JvmStatic
        private fun invalidCommandValues() =
            listOf(
                "",
                "0,1250,2500,3750,100",
                "5:0,1250,2500,3750,100",
                "L:0,1250,2500,3750,100",
                "4:bla",
                "4:0,1.25,2.5,3.75,0.1",
                "4,0,1250,2500,3750,100",
                "3:een,twee,drie,vier",
                "AL6:0,1250,2500,3750,100",
            )
    }
}
