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
class RspCommandResultHandlerTest {

    @MockK private lateinit var commandService: CommandService

    @MockK private lateinit var commandFeedbackService: CommandFeedbackService

    @InjectMockKs private lateinit var rspCommandResultHandler: RspCommandResultHandler

    @Test
    fun handleSuccess() {
        // arrange
        val command = CommandFactory.pendingPskCommand()
        every { commandService.saveCommand(any()) } answers { firstArg() }
        justRun { commandFeedbackService.sendSuccessFeedback(any()) }

        // act
        rspCommandResultHandler.handleSuccess(command)

        // assert
        assertThat(command.status == Command.CommandStatus.SUCCESSFUL)
        verify { commandService.saveCommand(command) }
        verify { commandFeedbackService.sendSuccessFeedback(command) }
    }

    @Test
    fun handleFailure() {
        // arrange
        val command = CommandFactory.pendingPskCommand()
        val message = MessageFactory.messageWithUrc(listOf("PSK:HSER"), "")
        every { commandService.saveCommand(any()) } answers { firstArg() }
        justRun { commandFeedbackService.sendErrorFeedback(any(), any()) }

        // act
        rspCommandResultHandler.handleFailure(command, message)

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
        val commandType = rspCommandResultHandler.forCommandType()

        // assert
        assertThat(commandType).isEqualTo(Command.CommandType.RSP)
    }

    @ParameterizedTest
    @MethodSource("hasSucceededTestSource")
    fun hasSucceeded(urcs: List<String>, downlink: String, expectedResult: Boolean) {
        // arrange
        val message = MessageFactory.messageWithUrc(urcs, downlink)

        // act
        val hasSucceeded = rspCommandResultHandler.hasSucceeded(TestConstants.DEVICE_ID, message)

        // assert
        assertThat(hasSucceeded).isEqualTo(expectedResult)
    }

    @ParameterizedTest
    @MethodSource("hasFailedTestSource")
    fun hasFailed(urcs: List<String>, downlink: String, expectedResult: Boolean) {
        // arrange
        val message = MessageFactory.messageWithUrc(urcs, downlink)

        // act
        val hasFailed = rspCommandResultHandler.hasFailed(TestConstants.DEVICE_ID, message)

        // assert
        assertThat(hasFailed).isEqualTo(expectedResult)
    }

    companion object {
        @JvmStatic
        fun hasSucceededTestSource(): Stream<Arguments> =
            Stream.of(
                Arguments.of(listOf<String>(), "CMD:RSP", true),
                Arguments.of(listOf("PSK:TMP"), "!PSK:######;CMD:RSP", true),
                Arguments.of(listOf("PSK:TMP"), "!CMD:RSP;PSK:######", true),
                Arguments.of(listOf("RSP:DLER"), "CMD:RSP", false),
                Arguments.of(listOf("RSP:DLER", "PSK:TMP"), "!CMD:RSP;PSK:######", false),
                Arguments.of(listOf("INIT", "WDR"), "0", false),
                Arguments.of(listOf("PSK:SET"), "0", false),
                Arguments.of(listOf("PSK:TMP", "PSK:SET"), "0", false),
            )

        @JvmStatic
        fun hasFailedTestSource(): Stream<Arguments> =
            Stream.of(
                Arguments.of(listOf("RSP:DLER"), "CMD:RSP", true),
                Arguments.of(listOf("RSP:DLER"), "CMD:RSP", true),
                Arguments.of(listOf("RSP:DLER", "PSK:TMP"), "!CMD:RSP;PSK:######", true),
                Arguments.of(listOf<String>(), "CMD:RSP", false),
                Arguments.of(listOf("PSK:TMP"), "!PSK:######;CMD:RSP", false),
                Arguments.of(listOf("PSK:TMP"), "!CMD:RSP;PSK:######", false),
                Arguments.of(listOf("PSK:DLER"), "!PSK:#####", false),
                Arguments.of(listOf("PSK:HSER"), "0", false),
                Arguments.of(listOf("INIT", "WDR"), "0", false),
                Arguments.of(listOf("PSK:TMP"), "!PSK:######", false),
                Arguments.of(listOf("PSK:SET"), "!PSK:#####SET", false),
            )
    }
}
