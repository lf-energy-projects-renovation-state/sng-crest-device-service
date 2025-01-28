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

    fun translateFromChannel(channel: String) =
        when (channel) {
            "AL0" -> "tamper"
            "AL1" -> "digital"
            "AL2" -> "T1"
            "AL3" -> "H1"
            "AL4" -> "1"
            "AL5" -> "2"
            "AL6" -> "3"
            "AL7" -> "4"
            else -> channel
        }
}
