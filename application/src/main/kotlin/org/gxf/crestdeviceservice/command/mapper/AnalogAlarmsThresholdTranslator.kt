// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.mapper

import org.gxf.crestdeviceservice.command.exception.CommandValidationException

object AnalogAlarmsThresholdTranslator {
    fun translatePortToChannel(port: String) =
        when (port) {
            "3" -> "AL6"
            "4" -> "AL7"
            else -> throw CommandValidationException("Device port unknown: $port")
        }
}
