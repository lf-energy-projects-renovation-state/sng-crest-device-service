// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.mapper

import kotlin.math.roundToInt

object AnalogAlarmThresholdCalculator {
    // volt = millibar / 500
    // payload value = volt * 200
    fun getPayloadFromMillibar(millibarValue: Int) = (millibarValue * 0.4).roundToInt()

    fun getMillibarFromPayload(payloadValue: Int) = payloadValue * 2.5.roundToInt()
}
