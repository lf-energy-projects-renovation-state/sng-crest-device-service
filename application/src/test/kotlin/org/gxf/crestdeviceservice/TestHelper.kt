// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.gxf.crestdeviceservice.command.entity.Command
import java.time.Instant
import org.mockito.kotlin.spy
import org.springframework.util.ResourceUtils
import java.util.UUID

object TestHelper {
    private val mapper = spy<ObjectMapper>()
    private val deviceId = "device-id"

    fun messageTemplate(): ObjectNode {
        val messageFile = ResourceUtils.getFile("classpath:message-template.json")
        return mapper.readTree(messageFile) as ObjectNode
    }

    fun commandsForDownlink() = listOf(
        Command(
            UUID.randomUUID(),
            deviceId,
            UUID.randomUUID(),
            Instant.now(),
            Command.CommandType.REBOOT,
            "reboot",
            Command.CommandStatus.IN_PROGRESS
        ),
        Command(
            UUID.randomUUID(),
            deviceId,
            UUID.randomUUID(),
            Instant.now(),
            Command.CommandType.FIRMWARE,
            "firmware",
            Command.CommandStatus.IN_PROGRESS
        )
    )

    fun externalCommand() = com.alliander.sng.Command.newBuilder()
        .setDeviceId(deviceId)
        .setCorrelationId(UUID.randomUUID())
        .setTimestamp(Instant.now())
        .setCommand(Command.CommandType.REBOOT.name)
        .setValue(null)
        .build()

    fun pendingCommandEntity() = Command(
        id = UUID.randomUUID(),
        deviceId = deviceId,
        correlationId = UUID.randomUUID(),
        timestampIssued = Instant.now(),
        type = Command.CommandType.REBOOT,
        commandValue = null,
        status = Command.CommandStatus.PENDING
    )
}
