// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.mapper

import com.alliander.sng.Command as ExternalCommand
import java.util.UUID
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.exception.CommandValidationException

object CommandMapper {
    fun externalCommandToCommandEntity(externalCommand: ExternalCommand, status: Command.CommandStatus): Command {
        try {
            return Command(
                id = UUID.randomUUID(),
                deviceId = externalCommand.deviceId,
                correlationId = externalCommand.correlationId,
                timestampIssued = externalCommand.timestamp,
                type = commandNameToType(externalCommand.command),
                status = status,
                commandValue = translateCommandValue(externalCommand)
            )
        } catch (exception: IllegalArgumentException) {
            throw CommandValidationException("Command unknown: ${externalCommand.command}")
        }
    }

    fun commandNameToType(command: String) = Command.CommandType.valueOf(command.uppercase())

    private fun translateCommandValue(command: ExternalCommand): String? {
        val analogAlarmThresholds = Command.CommandType.ANALOG_ALARM_THRESHOLDS.name.lowercase()
        return when (command.command) {
            analogAlarmThresholds -> translateAnalogAlarmsThresholdValue(command.value)
            else -> command.value
        }
    }

    private fun translateAnalogAlarmsThresholdValue(value: String): String {
        val port = value.substringBefore(':')
        val channel = translatePortToChannel(port)
        val pressureValues =
            value
                .substringAfter(':')
                .split(",")
                .map { it.toInt() }
                .map { AnalogAlarmThresholdCalculator.getPayloadFromMBar(it) }
                .joinToString(",")
        return "$channel:$pressureValues"
    }

    private fun translatePortToChannel(port: String) =
        when (port) {
            "3" -> "AL6"
            "4" -> "AL7"
            else -> throw CommandValidationException("Device port unknown: $port")
        }
}
