// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.mapper

import kotlin.math.roundToInt

object AnalogAlarmThresholdCalculator {
    // volt = mbar / 500
    // payload value = volt * 200
    fun getPayloadFromMBar(mBarValue: Int) = (mBarValue * 0.4).roundToInt() // todo range payload values

    fun getMBarFromPayload(payloadValue: Int) = payloadValue * 2.5.roundToInt()
}
