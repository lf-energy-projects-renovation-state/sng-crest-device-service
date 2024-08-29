package org.gxf.crestdeviceservice.command.mapper

import org.gxf.crestdeviceservice.command.entity.Command
import java.util.UUID
import com.alliander.sng.Command as ExternalCommand

object CommandMapper {
    fun externalCommandToCommandEntity(command: ExternalCommand, status: Command.CommandStatus) =
        Command(
            id = UUID.randomUUID(),
            deviceId = command.deviceId,
            correlationId = command.correlationId,
            timestampIssued = command.timestamp,
            type = Command.CommandType.valueOf(translateCommand(command.command)),
            status = status,
            commandValue = command.value
        )

    fun translateCommand(command: String) = command.trim('!').uppercase()
}