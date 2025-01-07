// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.generator

import org.gxf.crestdeviceservice.command.entity.Command
import org.springframework.stereotype.Component

@Component
class AnalogAlarmsSetThresholdPort3CommandGenerator : CommandGenerator {
    override fun getSupportedCommand() = Command.CommandType.ANALOG_ALARM_THRESHOLDS_PORT_3

    override fun generateCommandString(command: Command) = "${getSupportedCommand().downlink}:${command.commandValue}"
}
