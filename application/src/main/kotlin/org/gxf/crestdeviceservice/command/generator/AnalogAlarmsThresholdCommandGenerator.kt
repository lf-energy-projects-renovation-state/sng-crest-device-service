// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.generator

import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.exception.CommandValidationException
import org.gxf.crestdeviceservice.command.mapper.AnalogAlarmThresholdCalculator
import org.springframework.stereotype.Component

@Component
class AnalogAlarmsThresholdCommandGenerator : CommandGenerator {
    override fun getSupportedCommand() = Command.CommandType.ANALOG_ALARM_THRESHOLDS

    override fun generateCommandString(command: Command): String {
        val commandValue = command.commandValue!!
        val port = commandValue.substringBefore(':')
        val channel = translatePortToChannel(port)
        val pressureValues =
            commandValue
                .substringAfter(':')
                .split(",")
                .map { it.toInt() }
                .map { AnalogAlarmThresholdCalculator.getPayloadFromMillibar(it) }
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
