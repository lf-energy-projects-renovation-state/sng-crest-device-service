// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import java.time.Instant
import java.util.UUID
import org.gxf.crestdeviceservice.command.entity.Command
import org.mockito.kotlin.spy
import org.springframework.util.ResourceUtils

object TestHelper {
    private val mapper = spy<ObjectMapper>()
    const val DEVICE_ID = "device-id"

    fun messageTemplate(): ObjectNode {
        val messageFile = ResourceUtils.getFile("classpath:message-template.json")
        return mapper.readTree(messageFile) as ObjectNode
    }

    fun receivedRebootCommand() =
        com.alliander.sng.Command.newBuilder()
            .setDeviceId(DEVICE_ID)
            .setCorrelationId(UUID.randomUUID())
            .setTimestamp(Instant.now())
            .setCommand(Command.CommandType.REBOOT.name)
            .setValue(null)
            .build()!!

    fun pendingPskCommand() =
        Command(
            id = UUID.randomUUID(),
            deviceId = DEVICE_ID,
            correlationId = UUID.randomUUID(),
            timestampIssued = Instant.now(),
            type = Command.CommandType.PSK,
            commandValue = null,
            status = Command.CommandStatus.PENDING
        )

    fun pendingPskSetCommand() =
        Command(
            id = UUID.randomUUID(),
            deviceId = DEVICE_ID,
            correlationId = UUID.randomUUID(),
            timestampIssued = Instant.now(),
            type = Command.CommandType.PSK_SET,
            commandValue = null,
            status = Command.CommandStatus.PENDING
        )

    fun pendingRebootCommand() =
        Command(
            id = UUID.randomUUID(),
            deviceId = DEVICE_ID,
            correlationId = UUID.randomUUID(),
            timestampIssued = Instant.now(),
            type = Command.CommandType.REBOOT,
            commandValue = null,
            status = Command.CommandStatus.PENDING)

    fun pendingPskCommands() =
        listOf(pendingPskCommand(), pendingPskSetCommand())

    fun rebootCommandInProgress() =
        pendingRebootCommand().copy(status = Command.CommandStatus.IN_PROGRESS)

    fun pskCommandInProgress() =
        pendingPskCommand().copy(status = Command.CommandStatus.IN_PROGRESS)

    fun pskSetCommandInProgress() =
        pendingPskSetCommand().copy(status = Command.CommandStatus.IN_PROGRESS)

    fun pskCommandsInProgress() =
        listOf(pskCommandInProgress(), pskSetCommandInProgress())
}
