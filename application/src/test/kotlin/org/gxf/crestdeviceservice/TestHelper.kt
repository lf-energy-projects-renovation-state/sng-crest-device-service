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

    fun messageTemplate(): ObjectNode {
        val messageFile = ResourceUtils.getFile("classpath:message-template.json")
        return mapper.readTree(messageFile) as ObjectNode
    }

    fun commandsForDownlink() = listOf(
        Command(
            UUID.randomUUID(),
            "TST-01",
            UUID.randomUUID(),
            Instant.now(),
            Command.CommandType.REBOOT,
            "reboot",
            Command.CommandStatus.IN_PROGRESS
        ),
        Command(
            UUID.randomUUID(),
            "TST-01",
            UUID.randomUUID(),
            Instant.now(),
            Command.CommandType.FIRMWARE,
            "firmware",
            Command.CommandStatus.IN_PROGRESS
        )
    )
}
