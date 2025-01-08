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
class AnalogAlarmsSetThresholdPort4CommandGeneratorTest {
    private val generator = AnalogAlarmsSetThresholdPort4CommandGenerator()

    @Test
    fun shouldCreateACorrectCommandString() {
        val receivedValue = ANALOG_ALARM_THRESHOLDS_PAYLOAD
        val pendingCommand = CommandFactory.pendingAnalogAlarmThresholdsPort3Command(value = receivedValue)
        val expectedResult = "AL7:$ANALOG_ALARM_THRESHOLDS_PAYLOAD"

        val result = generator.generateCommandString(pendingCommand)

        assertThat(result).isEqualTo(expectedResult)
    }
}
