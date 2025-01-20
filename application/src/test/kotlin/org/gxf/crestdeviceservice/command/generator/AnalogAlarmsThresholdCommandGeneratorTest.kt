// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.generator

import io.mockk.junit5.MockKExtension
import java.util.stream.Stream
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.TestConstants.ANALOG_ALARM_THRESHOLDS_MBAR_PORT_3
import org.gxf.crestdeviceservice.TestConstants.ANALOG_ALARM_THRESHOLDS_MBAR_PORT_4
import org.gxf.crestdeviceservice.TestConstants.ANALOG_ALARM_THRESHOLDS_PAYLOAD_PORT_3
import org.gxf.crestdeviceservice.TestConstants.ANALOG_ALARM_THRESHOLDS_PAYLOAD_PORT_4
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@ExtendWith(MockKExtension::class)
class AnalogAlarmsThresholdCommandGeneratorTest {
    private val generator = AnalogAlarmsThresholdCommandGenerator()

    @ParameterizedTest
    @MethodSource("valueTranslations")
    fun shouldCreateACorrectCommandString(source: String, translation: String) {
        val pendingCommand = CommandFactory.pendingAnalogAlarmThresholdsCommand(value = source)

        val result = generator.generateCommandString(pendingCommand)

        assertThat(result).isEqualTo(translation)
    }

    companion object {
        @JvmStatic
        private fun valueTranslations() =
            Stream.of(
                Arguments.of(ANALOG_ALARM_THRESHOLDS_MBAR_PORT_3, ANALOG_ALARM_THRESHOLDS_PAYLOAD_PORT_3),
                Arguments.of(ANALOG_ALARM_THRESHOLDS_MBAR_PORT_4, ANALOG_ALARM_THRESHOLDS_PAYLOAD_PORT_4),
            )
    }
}
