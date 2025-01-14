// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.generator

import org.gxf.crestdeviceservice.command.entity.Command
import org.springframework.stereotype.Component

@Component
class AnalogAlarmsThresholdCommandGenerator : CommandGenerator {
    override fun getSupportedCommand() = Command.CommandType.ANALOG_ALARM_THRESHOLDS

    override fun generateCommandString(command: Command) = command.commandValue!!
}
