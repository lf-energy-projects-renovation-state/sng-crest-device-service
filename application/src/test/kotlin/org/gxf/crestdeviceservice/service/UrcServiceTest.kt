// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.alliander.sng.CommandStatus
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BaseJsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import java.util.stream.Stream
import org.gxf.crestdeviceservice.TestHelper
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.psk.service.PskService
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UrcServiceTest {
    private val pskService = mock<PskService>()
    private val commandService = mock<CommandService>()
    private val commandFeedbackService = mock<CommandFeedbackService>()
    private val urcService = UrcService(pskService, commandService, commandFeedbackService)
    private val mapper = spy<ObjectMapper>()

    companion object {
        private const val URC_FIELD = "URC"
        private const val DL_FIELD = "DL"
        private const val PSK_DOWNLINK =
            "!PSK:umU6KJ4g7Ye5ZU6o:4a3cfdd487298e2f048ebfd703a1da4800c18f2167b62192cf7dc9fd6cc4bcd3;PSK:umU6KJ4g7Ye5ZU6o:4a3cfdd487298e2f048ebfd703a1da4800c18f2167b62192cf7dc9fd6cc4bcd3:SET"
        private const val REBOOT_DOWNLINK = "!CMD:REBOOT"
        private const val DEVICE_ID = "867787050253370"

        @JvmStatic
        private fun containingPskErrorUrcs() =
            Stream.of(
                listOf("PSK:EQER"),
                listOf("PSK:DLNA"),
                listOf("PSK:DLER"),
                listOf("PSK:HSER"),
                listOf("PSK:CSER"),
                listOf("TS:ERR", "PSK:DLER"),
                listOf("PSK:HSER", "PSK:DLER"))

        @JvmStatic
        private fun containingErrorUrcs() =
            Stream.of(
                listOf("OTA:HSER", "MSI:DLNA"),
                listOf("TS:ERR"),
            )

        @JvmStatic
        private fun notContainingErrorUrcs() =
            Stream.of(
                listOf("INIT"),
                listOf("ENPD"),
                listOf("TEL:RBT"),
                listOf("JTR"),
                listOf("WDR"),
                listOf("BOR"),
                listOf("EXR"),
                listOf("POR"),
                listOf("INIT", "BOR", "POR"))
    }

    @Test
    fun shouldChangeActiveKeyWhenSuccessURCReceived() {
        val urcs = listOf("PSK:SET")
        interpretURCWhileNewKeyIsPending(urcs)
        verify(pskService).changeActiveKey(DEVICE_ID)
    }

    @ParameterizedTest(name = "should set pending key as invalid for {0}")
    @MethodSource("containingPskErrorUrcs")
    fun shouldSetPendingKeyAsInvalidWhenPskFailureURCReceived(urcs: List<String>) {
        interpretURCWhileNewKeyIsPending(urcs)
        verify(pskService).setPendingKeyAsInvalid(DEVICE_ID)
    }

    @ParameterizedTest(name = "should not set pending key as invalid for {0}")
    @MethodSource("notContainingErrorUrcs")
    fun shouldNotSetPendingKeyAsInvalidWhenOtherURCReceived(urcs: List<String>) {
        interpretURCWhileNewKeyIsPending(urcs)
        verify(pskService, times(0)).setPendingKeyAsInvalid(DEVICE_ID)
    }

    @ParameterizedTest(name = "should handle error urc for command")
    @MethodSource("containingErrorUrcs")
    fun handleErrorUrcForCommand(urcs: List<String>) {
        val commandInProgress = TestHelper.rebootCommandInProgress()
        val commandError = commandInProgress.copy(status = Command.CommandStatus.ERROR)
        val message = updateUrcInMessage(urcs, REBOOT_DOWNLINK)

        whenever(pskService.needsKeyChange(DEVICE_ID)).thenReturn(false)
        whenever(pskService.isPendingKeyPresent(DEVICE_ID)).thenReturn(false)
        whenever(commandService.getFirstCommandInProgressForDevice(DEVICE_ID))
            .thenReturn(commandInProgress)
        whenever(
                commandService.saveCommandWithNewStatus(
                    commandInProgress, Command.CommandStatus.ERROR))
            .thenReturn(commandError)

        urcService.interpretURCInMessage(DEVICE_ID, message)

        verify(commandService)
            .saveCommandWithNewStatus(commandInProgress, Command.CommandStatus.ERROR)
        verify(commandFeedbackService)
            .sendFeedback(eq(commandError), eq(CommandStatus.Error), any<String>())
    }

    @Test
    fun handleSuccessUrcForCommand() {
        val urcs = listOf("INIT", "WDR")
        val commandInProgress = TestHelper.rebootCommandInProgress()
        val commandSuccessful = commandInProgress.copy(status = Command.CommandStatus.SUCCESSFUL)
        val message = updateUrcInMessage(urcs, REBOOT_DOWNLINK)

        whenever(pskService.needsKeyChange(DEVICE_ID)).thenReturn(false)
        whenever(pskService.isPendingKeyPresent(DEVICE_ID)).thenReturn(false)
        whenever(commandService.getFirstCommandInProgressForDevice(DEVICE_ID))
            .thenReturn(commandInProgress)
        whenever(
                commandService.saveCommandWithNewStatus(
                    commandInProgress, Command.CommandStatus.SUCCESSFUL))
            .thenReturn(commandSuccessful)

        urcService.interpretURCInMessage(DEVICE_ID, message)

        verify(commandService)
            .saveCommandWithNewStatus(commandInProgress, Command.CommandStatus.SUCCESSFUL)
        verify(commandFeedbackService)
            .sendFeedback(eq(commandSuccessful), eq(CommandStatus.Successful), any<String>())
    }

    @Test
    fun shouldDoNothingIfUrcDoesNotConcernCommandInProgress() {
        val urcs = listOf("ENPD")
        val commandInProgress = TestHelper.rebootCommandInProgress()
        val message = updateUrcInMessage(urcs, REBOOT_DOWNLINK)

        whenever(pskService.needsKeyChange(DEVICE_ID)).thenReturn(false)
        whenever(pskService.isPendingKeyPresent(DEVICE_ID)).thenReturn(false)
        whenever(commandService.getFirstCommandInProgressForDevice(DEVICE_ID))
            .thenReturn(commandInProgress)

        urcService.interpretURCInMessage(DEVICE_ID, message)

        verify(commandService, times(0)).saveCommandEntity(any<Command>())
        verify(commandFeedbackService, times(0))
            .sendFeedback(any<Command>(), any<CommandStatus>(), any<String>())
    }

    private fun interpretURCWhileNewKeyIsPending(urcs: List<String>) {
        whenever(pskService.needsKeyChange(DEVICE_ID)).thenReturn(false)
        whenever(pskService.isPendingKeyPresent(DEVICE_ID)).thenReturn(true)

        val message = updateUrcInMessage(urcs, PSK_DOWNLINK)

        urcService.interpretURCInMessage(DEVICE_ID, message)
    }

    private fun updateUrcInMessage(urcs: List<String>, downlink: String): JsonNode {
        val message = TestHelper.messageTemplate()
        val urcFieldValue = urcFieldValue(urcs, downlink)

        message.replace(URC_FIELD, urcFieldValue)
        return message
    }

    private fun urcFieldValue(urcs: List<String>, downlink: String): ArrayNode? {
        val urcNodes = urcs.map { urc -> TextNode(urc) }
        val downlinkNode =
            ObjectNode(JsonNodeFactory.instance, mapOf(DL_FIELD to TextNode(downlink)))
        val urcsPlusReceivedDownlink: MutableList<BaseJsonNode> = mutableListOf()
        urcsPlusReceivedDownlink.addAll(urcNodes)
        urcsPlusReceivedDownlink.add(downlinkNode)
        val urcFieldValue = mapper.valueToTree<ArrayNode>(urcsPlusReceivedDownlink)
        return urcFieldValue
    }
}
