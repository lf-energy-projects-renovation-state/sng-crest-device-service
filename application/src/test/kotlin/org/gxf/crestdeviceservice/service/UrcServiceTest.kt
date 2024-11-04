// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import java.util.stream.Stream
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.TestConstants
import org.gxf.crestdeviceservice.TestHelper
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.psk.service.PskService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(MockKExtension::class)
class UrcServiceTest {
    @MockK private lateinit var pskService: PskService
    @MockK private lateinit var commandService: CommandService
    @MockK private lateinit var commandFeedbackService: CommandFeedbackService

    @InjectMockKs private lateinit var urcService: UrcService

    companion object {
        private const val URC_FIELD = "URC"
        private const val DL_FIELD = "DL"
        private val PSK_DOWNLINK =
            "!PSK:umU6KJ4g7Ye5ZU6o:4a3cfdd487298e2f048ebfd703a1da4800c18f2167b62192cf7dc9fd6cc4bcd3;" +
                ";PSK:umU6KJ4g7Ye5ZU6o:4a3cfdd487298e2f048ebfd703a1da4800c18f2167b62192cf7dc9fd6cc4bcd3:SET"
        private const val REBOOT_DOWNLINK = "!CMD:REBOOT"
        private const val DEVICE_ID = TestConstants.DEVICE_ID

        @JvmStatic
        private fun containingPskErrorUrcs() =
            Stream.of(
                listOf("PSK:DLER"),
                listOf("PSK:HSER"),
                listOf("TS:ERR", "PSK:DLER"),
                listOf("PSK:DLER", "PSK:EQER")
            )

        @JvmStatic
        private fun notContainingPskUrcs() =
            Stream.of(
                listOf("INIT"),
                listOf("ENPD"),
                listOf("TEL:RBT"),
                listOf("JTR"),
                listOf("WDR"),
                listOf("BOR"),
                listOf("EXR"),
                listOf("POR"),
                listOf("INIT", "BOR", "POR")
            )
    }

    @Test
    fun shouldChangeActiveKeyWhenSuccessUrcReceived() {
        val urcs = listOf("PSK:TMP", "PSK:SET")
        val pskCommandInProgress = CommandFactory.pskCommandInProgress()
        val pskSetCommandInProgress = CommandFactory.pskSetCommandInProgress()
        val pskCommandsInProgress = listOf(pskCommandInProgress, pskSetCommandInProgress)
        val message = updateUrcInMessage(urcs, PSK_DOWNLINK)

        every { pskService.isPendingPskPresent(DEVICE_ID) } returns true
        every { commandService.getAllCommandsInProgressForDevice(DEVICE_ID) } returns pskCommandsInProgress
        every { commandService.saveCommand(any()) } answers { firstArg() }
        justRun { pskService.changeActiveKey(any()) }
        justRun { commandFeedbackService.sendSuccessFeedback(any()) }

        urcService.interpretUrcsInMessage(DEVICE_ID, message)

        verify { pskService.changeActiveKey(DEVICE_ID) }

        assertThat(pskCommandInProgress.status).isEqualTo(Command.CommandStatus.SUCCESSFUL)
        assertThat(pskSetCommandInProgress.status).isEqualTo(Command.CommandStatus.SUCCESSFUL)
    }

    @ParameterizedTest(name = "should set pending key as invalid for {0}")
    @MethodSource("containingPskErrorUrcs")
    fun shouldSetPendingKeyAsInvalidWhenPskFailureUrcReceived(urcs: List<String>) {
        val pskCommandInProgress = CommandFactory.pskCommandInProgress()
        val pskSetCommandInProgress = CommandFactory.pskSetCommandInProgress()

        every { pskService.isPendingPskPresent(DEVICE_ID) } returns true
        every { //
            commandService.getAllCommandsInProgressForDevice(DEVICE_ID)
        } returns listOf(pskCommandInProgress, pskSetCommandInProgress)
        every { commandService.saveCommand(any()) } answers { firstArg() }
        justRun { pskService.setPendingKeyAsInvalid(any()) }
        justRun { commandFeedbackService.sendErrorFeedback(any(), any()) }

        val message = updateUrcInMessage(urcs, PSK_DOWNLINK)

        urcService.interpretUrcsInMessage(DEVICE_ID, message)

        verify { pskService.setPendingKeyAsInvalid(DEVICE_ID) }
        verify(exactly = 2) { commandService.saveCommand(any()) }
        verify(exactly = 2) { commandFeedbackService.sendErrorFeedback(any(), any()) }

        assertThat(pskCommandInProgress.status).isEqualTo(Command.CommandStatus.ERROR)
        assertThat(pskSetCommandInProgress.status).isEqualTo(Command.CommandStatus.ERROR)
    }

    @ParameterizedTest(name = "should not set pending key as invalid for {0}")
    @MethodSource("notContainingPskUrcs")
    fun shouldNotSetPendingKeyAsInvalidWhenOtherUrcReceived(urcs: List<String>) {
        val pskCommands = CommandFactory.pskCommandsInProgress()

        every { pskService.isPendingPskPresent(DEVICE_ID) } returns true
        every { commandService.getAllCommandsInProgressForDevice(DEVICE_ID) } returns pskCommands

        val message = updateUrcInMessage(urcs, PSK_DOWNLINK)

        urcService.interpretUrcsInMessage(DEVICE_ID, message)

        verify(exactly = 0) { pskService.setPendingKeyAsInvalid(DEVICE_ID) }
    }

    @Test
    fun handleSuccessUrcForRebootCommand() {
        val urcs = listOf("INIT", "WDR")
        val commandInProgress = CommandFactory.rebootCommandInProgress()
        val message = updateUrcInMessage(urcs, REBOOT_DOWNLINK)

        every { pskService.isPendingPskPresent(DEVICE_ID) } returns false
        every { commandService.getAllCommandsInProgressForDevice(DEVICE_ID) } returns listOf(commandInProgress)
        every { commandService.saveCommand(any()) } answers { firstArg() }
        justRun { commandFeedbackService.sendSuccessFeedback(any()) }

        urcService.interpretUrcsInMessage(DEVICE_ID, message)

        verify { commandService.saveCommand(commandInProgress) }
        verify { commandFeedbackService.sendSuccessFeedback(any()) }

        assertThat(commandInProgress.status).isEqualTo(Command.CommandStatus.SUCCESSFUL)
    }

    @Test
    fun shouldDoNothingIfUrcDoesNotConcernCommandInProgress() {
        val urcs = listOf("ENPD")
        val commandInProgress = CommandFactory.rebootCommandInProgress()
        val message = updateUrcInMessage(urcs, REBOOT_DOWNLINK)

        every { pskService.isPendingPskPresent(DEVICE_ID) } returns false
        every { commandService.getAllCommandsInProgressForDevice(DEVICE_ID) } returns listOf(commandInProgress)

        urcService.interpretUrcsInMessage(DEVICE_ID, message)

        verify(exactly = 0) { commandService.saveCommands(any(), any()) }
        verify { commandFeedbackService wasNot Called }
    }

    @ParameterizedTest(name = "should do nothing when downlink is {0}")
    @ValueSource(strings = ["0", "", " ", "\n"])
    fun shouldDoNothingWhenDownlinkIsBlank(downlink: String) {
        val urcs = listOf("INIT")
        val message = updateUrcInMessage(urcs, downlink)

        every { commandService.getAllCommandsInProgressForDevice(DEVICE_ID) } returns listOf()

        urcService.interpretUrcsInMessage(DEVICE_ID, message)
    }

    private fun updateUrcInMessage(urcs: List<String>, downlink: String): JsonNode {
        val urcNodes = urcs.map { urc -> TextNode(urc) }
        val downlinkNode = ObjectNode(JsonNodeFactory.instance, mapOf(DL_FIELD to TextNode(downlink)))

        val urcFieldValue: ArrayNode = ObjectMapper().valueToTree(urcNodes + listOf(downlinkNode))

        val message = TestHelper.messageTemplate()
        message.replace(URC_FIELD, urcFieldValue)

        return message
    }
}
