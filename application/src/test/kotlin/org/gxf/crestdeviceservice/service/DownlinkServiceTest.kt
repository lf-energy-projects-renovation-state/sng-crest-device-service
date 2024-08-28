// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.TestHelper
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandService
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
    private val downLinkService = DownlinkService(pskService, commandService)
    private val message = TestHelper.messageTemplate()

    companion object {
        private const val IDENTITY = "867787050253370"
    }

    @Test
    fun shouldReturnPskDownlinkWhenThereIsANewPsk() {
        val expectedKey = "key"
        val expectedHash = "ad165b11320bc91501ab08613cc3a48a62a6caca4d5c8b14ca82cc313b3b96cd"
        val psk =
            PreSharedKey(
                IDENTITY, 1, Instant.now(), expectedKey, "secret", PreSharedKeyStatus.PENDING)

        whenever(pskService.keyCanBeChanged(IDENTITY)).thenReturn(true)
        whenever(pskService.setPskToPendingForDevice(IDENTITY)).thenReturn(psk)

        val result = downLinkService.getDownlinkForDevice(IDENTITY, message)

        // Psk command is formatted as: PSK:[Key]:[Hash];PSK:[Key]:[Hash]:SET
        assertThat(result)
            .isEqualTo("!PSK:${expectedKey}:${expectedHash};PSK:${expectedKey}:${expectedHash}:SET")
    }

    @Test
    fun shouldSendFirstPendingCommandIfNoCommandInProgress() {
        val pendingCommand = TestHelper.pendingRebootCommand()
        whenever(pskService.keyCanBeChanged(IDENTITY)).thenReturn(false)
        whenever(commandService.getFirstPendingCommandForDevice(IDENTITY))
            .thenReturn(pendingCommand)
        whenever(commandService.getFirstCommandInProgressForDevice(IDENTITY)).thenReturn(null)
        whenever(
                commandService.saveCommandWithNewStatus(
                    pendingCommand, Command.CommandStatus.IN_PROGRESS))
            .thenReturn(pendingCommand.copy(status = Command.CommandStatus.IN_PROGRESS))

        val result = downLinkService.getDownlinkForDevice(IDENTITY, message)

        val expectedDownlink = "!CMD:REBOOT"
        assertThat(result).isEqualTo(expectedDownlink)
    }

    @Test
    fun shouldReturnNoActionDownlinkWhenThereIsNoNewPskAndACommandIsInProgress() {
        whenever(pskService.keyCanBeChanged(IDENTITY)).thenReturn(false)
        whenever(commandService.getFirstPendingCommandForDevice(IDENTITY)).thenReturn(null)
        whenever(commandService.getFirstCommandInProgressForDevice(IDENTITY))
            .thenReturn(TestHelper.rebootCommandInProgress())

        val result = downLinkService.getDownlinkForDevice(IDENTITY, message)

        assertThat(result).isEqualTo("0")
    }

    @Test
    fun shouldReturnNoActionDownlinkWhenThereIsNoNewPskOrPendingCommand() {
        whenever(pskService.keyCanBeChanged(IDENTITY)).thenReturn(false)

        val result = downLinkService.getDownlinkForDevice(IDENTITY, message)

        assertThat(result).isEqualTo("0")
    }

    @ParameterizedTest
    @CsvSource(
        "1234567890123456,ce2eca02d7ce354830eae7dd3b140755334f9c00582a53044655adde22126071,secret",
        "1234567890123456,78383f73855e7595f8d31ee7cabdf854bc4e70d036f225f8d144d566083c7d01,different-secret",
        "6543210987654321,5e15cf0f8a55b58a54f51dda17c1d1645ebc145f912888ec2e02a55d7b7baea4,secret",
        "6543210987654321,64904d94590a354cecd8e65630289bcc22103c07b08c009b0b12a8ef0d58af9d,different-secret")
    fun shouldCreateACorrectPskCommandoWithHash(
        key: String,
        expectedHash: String,
        usedSecret: String
    ) {
        val preSharedKey =
            PreSharedKey("identity", 0, Instant.now(), key, usedSecret, PreSharedKeyStatus.PENDING)

        val result = downLinkService.createPskSetCommand(preSharedKey)

        // Psk command is formatted as: PSK:[Key]:[Hash];PSK:[Key]:[Hash]:SET
        assertThat(result).isEqualTo("!PSK:${key}:${expectedHash};PSK:${key}:${expectedHash}:SET")
    }
}
