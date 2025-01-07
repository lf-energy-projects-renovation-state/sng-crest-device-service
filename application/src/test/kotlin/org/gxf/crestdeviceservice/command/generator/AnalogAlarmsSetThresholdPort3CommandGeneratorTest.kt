// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.generator

import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.CommandFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AnalogAlarmsSetThresholdPort3CommandGeneratorTest {
    private val generator = AnalogAlarmsSetThresholdPort3CommandGenerator()

    @Test
    fun shouldCreateACorrectCommandString() {
        val receivedValue = "0.00,1.25,2.50,3.75,0.10"
        val pendingCommand = CommandFactory.pendingAnalogAlarmThresholdsPort3Command(value = receivedValue)
        val expectedResult = "AL6:0.00,1.25,2.50,3.75,0.10"

        val result = generator.generateCommandString(pendingCommand)

        assertThat(result).isEqualTo(expectedResult)
    }
}
