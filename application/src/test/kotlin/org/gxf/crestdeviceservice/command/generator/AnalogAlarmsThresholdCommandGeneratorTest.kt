// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.generator

import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.TestConstants.ANALOG_ALARM_THRESHOLDS_PAYLOAD
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AnalogAlarmsThresholdCommandGeneratorTest {
    private val generator = AnalogAlarmsThresholdCommandGenerator()

    @Test
    fun shouldCreateACorrectCommandString() {
        val receivedValue = ANALOG_ALARM_THRESHOLDS_PAYLOAD
        val pendingCommand = CommandFactory.pendingAnalogAlarmThresholdsCommand(value = receivedValue)

        val result = generator.generateCommandString(pendingCommand)

        assertThat(result).isEqualTo(receivedValue)
    }
}
