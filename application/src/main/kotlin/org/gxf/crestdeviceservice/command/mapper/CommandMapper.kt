// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.mapper

import com.alliander.sng.Command as ExternalCommand
import java.util.UUID
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.exception.CommandValidationException

object CommandMapper {
    fun externalCommandToCommandEntity(command: ExternalCommand, status: Command.CommandStatus): Command {
        try {
            Command(
                id = UUID.randomUUID(),
                deviceId = command.deviceId,
                correlationId = command.correlationId,
                timestampIssued = command.timestamp,
                type = Command.CommandType.valueOf(translateCommand(command.command)),
                status = status,
                commandValue = command.value
            )
        }  catch (exception: IllegalArgumentException) {
            throw CommandValidationException("Command unknown")
        }
    }

    fun translateCommand(command: String) = command.trim('!').uppercase()
}
