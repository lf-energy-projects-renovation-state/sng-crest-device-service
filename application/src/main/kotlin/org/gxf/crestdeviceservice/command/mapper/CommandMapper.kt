// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.mapper

import com.alliander.sng.Command as ExternalCommand
import java.util.UUID
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.exception.CommandValidationException

private const val ANALOG_ALARM_THRESHOLDS_PORT_3 = "analog_alarm_thresholds_port_3"
private const val ANALOG_ALARM_THRESHOLDS_PORT_4 = "analog_alarm_thresholds_port_4"

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
        } catch (exception: IllegalStateException) {
            // for port number in value todo
            throw CommandValidationException("Command value unknown: ${externalCommand.value}")
        }
    }

    fun commandNameToType(command: String) = Command.CommandType.valueOf(command.uppercase())

    private fun translateCommandValue(command: ExternalCommand) =
        when (command.command) {
            ANALOG_ALARM_THRESHOLDS_PORT_3,
            ANALOG_ALARM_THRESHOLDS_PORT_4 -> translateAnalogAlarmsThresholdValue(command.value)
            else -> command.value
        }

    private fun translateAnalogAlarmsThresholdValue(value: String) =
        value
            .split(",")
            .map { it.toDouble() }
            .map { AnalogAlarmThresholdCalculator.getPayloadFromBar(it) }
            .joinToString { "," }
}
