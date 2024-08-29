package org.gxf.crestdeviceservice.command.mapper

import com.alliander.sng.Command as ExternalCommand
import com.alliander.sng.CommandFeedback
import com.alliander.sng.CommandStatus as ExternalCommandStatus
import org.gxf.crestdeviceservice.command.entity.Command
import java.time.Instant

object CommandFeedbackMapper {
    fun externalCommandToCommandFeedback(command: ExternalCommand, status: ExternalCommandStatus, message: String) =
        CommandFeedback.newBuilder()
            .setDeviceId(command.deviceId)
            .setCorrelationId(command.correlationId)
            .setTimestampStatus(Instant.now())
            .setStatus(status)
            .setMessage(message)
            .build()

    fun commandEntityToCommandFeedback(command: Command, status: ExternalCommandStatus, message: String) =
        CommandFeedback.newBuilder()
            .setDeviceId(command.deviceId)
            .setCorrelationId(command.correlationId)
            .setTimestampStatus(Instant.now())
            .setStatus(status)
            .setMessage(message)
            .build()
}