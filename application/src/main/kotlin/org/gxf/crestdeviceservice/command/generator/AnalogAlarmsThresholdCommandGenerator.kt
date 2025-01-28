// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.generator

import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.mapper.AnalogAlarmThresholdCalculator
import org.gxf.crestdeviceservice.command.mapper.AnalogAlarmsThresholdTranslator
import org.springframework.stereotype.Component

@Component
class AnalogAlarmsThresholdCommandGenerator : CommandGenerator {
    override fun getSupportedCommand() = Command.CommandType.ANALOG_ALARM_THRESHOLDS

    override fun generateCommandString(command: Command): String {
        val commandValue = command.commandValue!!
        val port = commandValue.substringBefore(':')
        val channel = AnalogAlarmsThresholdTranslator.translatePortToChannel(port)
        val pressureValues =
            commandValue
                .substringAfter(':')
                .split(",")
                .map { it.toInt() }
                .map { AnalogAlarmThresholdCalculator.getPayloadFromMillibar(it) }
                .joinToString(",")
        return "$channel:$pressureValues"
    }
}
