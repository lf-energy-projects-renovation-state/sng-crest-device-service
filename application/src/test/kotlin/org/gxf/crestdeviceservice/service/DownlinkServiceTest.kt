// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.TestHelper
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.config.MessageProperties
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.gxf.crestdeviceservice.psk.service.PskService
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DownlinkServiceTest {
    private val pskService = mock<PskService>()
    private val commandService = mock<CommandService>()
    private val messageProperties = MessageProperties(1024)
    private val downLinkService = DownlinkService(pskService, commandService, messageProperties)
    private val message = TestHelper.messageTemplate()
    private val deviceId = TestHelper.DEVICE_ID

    @Test
    fun shouldReturnPskDownlinkWhenThereIsANewPsk() {
        val expectedKey = "key"
        val expectedHash = "ad165b11320bc91501ab08613cc3a48a62a6caca4d5c8b14ca82cc313b3b96cd"
        val pskReady =
            PreSharedKey(
                deviceId, 1, Instant.now(), expectedKey, "secret", PreSharedKeyStatus.READY)
        val pskPending =
            PreSharedKey(
                deviceId, 1, Instant.now(), expectedKey, "secret", PreSharedKeyStatus.PENDING)
        val pskCommandPending = TestHelper.pendingPskCommand()
        val pskSetCommandPending = TestHelper.pendingPskSetCommand()
        val pskCommandsPending = listOf(pskCommandPending, pskSetCommandPending)
        val pskCommandInProgress =
            pskCommandPending.copy(status = Command.CommandStatus.IN_PROGRESS)
        val pskSetCommandInProgress =
            pskSetCommandPending.copy(status = Command.CommandStatus.IN_PROGRESS)

        whenever(commandService.getAllPendingCommandsForDevice(deviceId))
            .thenReturn(pskCommandsPending)
        whenever(pskService.readyForPskSetCommand(deviceId)).thenReturn(true)
        whenever(
                commandService.saveCommandWithNewStatus(
                    pskCommandPending, Command.CommandStatus.IN_PROGRESS))
            .thenReturn(pskCommandInProgress)
        whenever(pskService.getCurrentReadyPsk(deviceId)).thenReturn(pskReady)
        whenever(
                commandService.saveCommandWithNewStatus(
                    pskSetCommandPending, Command.CommandStatus.IN_PROGRESS))
            .thenReturn(pskSetCommandInProgress)
        whenever(pskService.setPskToPendingForDevice(deviceId)).thenReturn(pskPending)

        val result = downLinkService.getDownlinkForDevice(deviceId, message)

        // Psk command is formatted as: PSK:[Key]:[Hash];PSK:[Key]:[Hash]:SET
        assertThat(result)
            .isEqualTo("!PSK:${expectedKey}:${expectedHash};PSK:${expectedKey}:${expectedHash}:SET")
    }

    @Test
    fun shouldSendPendingCommandsIfNoCommandInProgress() {
        val rebootCommand = TestHelper.pendingRebootCommand()
        val pendingCommands = listOf(rebootCommand)
        whenever(pskService.readyForPskSetCommand(deviceId)).thenReturn(false)
        whenever(commandService.getAllPendingCommandsForDevice(deviceId))
            .thenReturn(pendingCommands)
        whenever(commandService.getFirstCommandInProgressForDevice(deviceId)).thenReturn(null)
        whenever(
                commandService.saveCommandWithNewStatus(
                    rebootCommand, Command.CommandStatus.IN_PROGRESS))
            .thenReturn(rebootCommand.copy(status = Command.CommandStatus.IN_PROGRESS))

        val result = downLinkService.getDownlinkForDevice(deviceId, message)

        val expectedDownlink = "!CMD:REBOOT"
        assertThat(result).isEqualTo(expectedDownlink)
    }

    @Test
    fun shouldReturnNoActionDownlinkWhenThereIsNoNewPskAndACommandIsInProgress() {
        whenever(pskService.readyForPskSetCommand(deviceId)).thenReturn(false)
        whenever(commandService.getAllPendingCommandsForDevice(deviceId)).thenReturn(listOf())
        whenever(commandService.getFirstCommandInProgressForDevice(deviceId))
            .thenReturn(TestHelper.rebootCommandInProgress())

        val result = downLinkService.getDownlinkForDevice(deviceId, message)

        assertThat(result).isEqualTo("0")
    }

    @Test
    fun shouldReturnNoActionDownlinkWhenThereIsNoNewPskOrPendingCommand() {
        whenever(pskService.readyForPskSetCommand(deviceId)).thenReturn(false)

        val result = downLinkService.getDownlinkForDevice(deviceId, message)

        assertThat(result).isEqualTo("0")
    }

    @ParameterizedTest
    @CsvSource(
        "1234567890123456,ce2eca02d7ce354830eae7dd3b140755334f9c00582a53044655adde22126071,secret",
        "1234567890123456,78383f73855e7595f8d31ee7cabdf854bc4e70d036f225f8d144d566083c7d01,different-secret",
        "6543210987654321,5e15cf0f8a55b58a54f51dda17c1d1645ebc145f912888ec2e02a55d7b7baea4,secret",
        "6543210987654321,64904d94590a354cecd8e65630289bcc22103c07b08c009b0b12a8ef0d58af9d,different-secret")
    fun shouldCreateACorrectPskSetCommandWithHash(
        key: String,
        expectedHash: String,
        usedSecret: String
    ) {
        val preSharedKey =
            PreSharedKey("identity", 0, Instant.now(), key, usedSecret, PreSharedKeyStatus.PENDING)

        val result = downLinkService.createPskSetCommand(preSharedKey)

        // PSK:[Key]:[Hash]:SET
        assertThat(result).isEqualTo("PSK:${key}:${expectedHash}:SET")
    }

    @Test
    fun firstCommandFitsMaxMessageSize() {
        downLinkService.downlinkCumulative = ""
        val downlinkToAdd = "!CMD:REBOOT"

        val result = downLinkService.fitsInMaxMessageSize(downlinkToAdd)

        assertThat(result).isTrue()
        assertThat(downLinkService.downlinkCumulative).isEqualTo(downlinkToAdd)
    }

    @Test
    fun thirdCommandFitsMaxMessageSize() {
        val downlinkExisting = "!CMD:REBOOT;!CMD:REBOOT"
        downLinkService.downlinkCumulative = downlinkExisting
        val downlinkToAdd = "!CMD:REBOOT"

        val result = downLinkService.fitsInMaxMessageSize(downlinkToAdd)

        assertThat(result).isTrue()
        assertThat(downLinkService.downlinkCumulative)
            .isEqualTo(downlinkExisting.plus(";$downlinkToAdd"))
    }

    @Test
    fun doesNotFitMaxMessageSize() {
        val downlinkExisting =
            "PSK:1234567890123456:ce2eca02d7ce354830eae7dd3b140755334f9c00582a53044655adde22126071;" +
                "PSK:1234567890123456:ce2eca02d7ce354830eae7dd3b140755334f9c00582a53044655adde22126071:SET;" +
                "OTA0000^IO>dUJt\"`!&`;3d5CdvyU6^v1Kn)OEu?2GK\"yK\"5OELysFlnkY49nUE1GiM4wQfm<PdQn01lJ@Ab^uW4S_HT@Rz`Ezzh&^FaT%W4wQfm4wL{OfRjNDfiMUVaX<hG@S(tj;h5kKlz;=Az<`l<5OGidK@EWsBEZm-K@H3!;qWP#I{;" +
                "OTA004E5OELysFlnkY49nUE1GiM4wQfm<PdQn01lJ@Ab^uW4S_HT@Rz`Ezzh&^FaT%W4wQfm4wL{OfRjNDfiMUVaX<hG@S(tj;h5kKlz;=Az<`l<5OGidK@EWsBEZm-K@H3!;qWP#JM&xE{\$3Bi&BI%Tvw4VdaJ\$I-w\"%d5\$}Oa4EI`MX`T:DONE;" +
                "OTA0000^IO>dUJt\"`!&`;3d5CdvyU6^v1Kn)OEu?2GK\"yK\"5OELysFlnkY49nUE1GiM4wQfm<PdQn01lJ@Ab^uW4S_HT@Rz`Ezzh&^FaT%W4wQfm4wL{OfRjNDfiMUVaX<hG@S(tj;h5kKlz;=Az<`l<5OGidK@EWsBEZm-K@H3!;qWP#I{;" +
                "PSK:1234567890123456:ce2eca02d7ce354830eae7dd3b140755334f9c00582a53044655adde22126071:SET"
        val downlinkToAdd = "!CMD:REBOOT"
        downLinkService.downlinkCumulative = downlinkExisting

        val result = downLinkService.fitsInMaxMessageSize(downlinkToAdd)

        assertThat(result).isFalse()
        assertThat(downLinkService.downlinkCumulative).isEqualTo(downlinkExisting)
    }
}
