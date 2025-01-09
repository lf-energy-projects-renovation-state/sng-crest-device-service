// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.mapper

import kotlin.math.roundToInt

object AnalogAlarmThresholdCalculator {
    // volt = bar * 2
    // payload value = volt * 200
    fun getPayloadFromBar(barValue: Double) = (barValue * 2 * 200).roundToInt() // todo range payload values

    fun getBarFromPayload(payloadValue: Int) = payloadValue / 2.0 / 200.0
}
