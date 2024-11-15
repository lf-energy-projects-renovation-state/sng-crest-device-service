// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.resulthandler

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import java.util.stream.Stream
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.MessageFactory
import org.gxf.crestdeviceservice.TestConstants
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@ExtendWith(MockKExtension::class)
class FirmwareCommandResultHandlerTest {

    @MockK private lateinit var commandService: CommandService

    @MockK private lateinit var commandFeedbackService: CommandFeedbackService

    @InjectMockKs private lateinit var firmwareCommandResultHandler: FirmwareCommandResultHandler

    @Test
    fun handleSuccess() {
        // arrange
        val command = CommandFactory.firmwareCommandInProgress()
        every { commandService.saveCommand(any()) } answers { firstArg() }
        justRun { commandFeedbackService.sendSuccessFeedback(any()) }

        // act
        firmwareCommandResultHandler.handleSuccess(command)

        // assert
        assertThat(command.status == Command.CommandStatus.SUCCESSFUL)
        verify { commandService.saveCommand(command) }
        verify { commandFeedbackService.sendSuccessFeedback(command) }
    }

    @Test
    fun handleFailure() {
        // arrange
        val command = CommandFactory.firmwareCommandInProgress()
        val message = MessageFactory.messageWithUrc(listOf("OTA:HSER"), "")
        every { commandService.saveCommand(any()) } answers { firstArg() }
        justRun { commandFeedbackService.sendErrorFeedback(any(), any()) }

        // act
        firmwareCommandResultHandler.handleFailure(command, message)

        // assert
        assertThat(command.status == Command.CommandStatus.ERROR)
        verify { commandService.saveCommand(command) }
        verify {
            commandFeedbackService.sendErrorFeedback(command, match { error -> error.contains("SHA256 hash error") })
        }
    }

    @Test
    fun forCommandType() {
        // nothing to arrange

        // act
        val commandType = firmwareCommandResultHandler.forCommandType()

        // assert
        assertThat(commandType).isEqualTo(Command.CommandType.FIRMWARE)
    }

    @ParameterizedTest
    @MethodSource("hasSucceededTestSource")
    fun hasSucceeded(urcs: List<String>, downlink: String, expectedResult: Boolean) {
        // arrange
        val message = MessageFactory.messageWithUrc(urcs, downlink)

        // act
        val hasSucceeded = firmwareCommandResultHandler.hasSucceeded(TestConstants.DEVICE_ID, message)

        // assert
        assertThat(hasSucceeded).isEqualTo(expectedResult)
    }

    @ParameterizedTest
    @MethodSource("hasFailedTestSource")
    fun hasFailed(urcs: List<String>, downlink: String, expectedResult: Boolean) {
        // arrange
        val message = MessageFactory.messageWithUrc(urcs, downlink)

        // act
        val hasFailed = firmwareCommandResultHandler.hasFailed(TestConstants.DEVICE_ID, message)

        // assert
        assertThat(hasFailed).isEqualTo(expectedResult)
    }

    companion object {
        @JvmStatic
        fun hasSucceededTestSource(): Stream<Arguments> =
            Stream.of(
                Arguments.of(listOf("OTA:SUC"), "0", true),
                Arguments.of(listOf("INIT", "WDR"), "0", false),
                Arguments.of(listOf("INIT", "WDR", "OTA:SUC"), "0", true),
            )

        @JvmStatic
        fun hasFailedTestSource(): Stream<Arguments> =
            Stream.of(
                Arguments.of(listOf("OTA:SUC"), "0", false),
                Arguments.of(listOf("INIT", "WDR"), "0", false),
                Arguments.of(listOf("OTA:CSER"), "0", true),
                Arguments.of(listOf("OTA:HSER"), "0", true),
                Arguments.of(listOf("OTA:RST"), "0", true),
                Arguments.of(listOf("OTA:SWNA"), "0", true),
                Arguments.of(listOf("OTA:FLER"), "0", true),
            )
    }
}
