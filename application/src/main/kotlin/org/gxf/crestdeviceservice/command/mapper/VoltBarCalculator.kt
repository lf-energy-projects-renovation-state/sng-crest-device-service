// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.mapper

object VoltBarCalculator {
    fun getBarToVolt(barValue: Double) = barValue * 2

    fun getVoltToBar(voltValue: Double) = voltValue * 0.5
}
