// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.feedbackgenerator

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.MessageFactory
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.AlarmsInfoService
import org.gxf.crestdeviceservice.model.AlarmsInfo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class InfoAlarmsFeedbackGeneratorTest {
    @MockK private lateinit var alarmsInfoService: AlarmsInfoService

    @InjectMockKs private lateinit var infoAlarmsFeedbackGenerator: InfoAlarmsFeedbackGenerator

    @Test
    fun getSupportedCommandType() {
        val result = infoAlarmsFeedbackGenerator.supportedCommandType

        assertThat(result).isEqualTo(Command.CommandType.INFO_ALARMS)
    }

    @Test
    fun generateFeedback() {
        val message = MessageFactory.messageTemplate()
        val alarmsInfo = AlarmsInfo()
        val expected = alarmsInfo.toString()
        every { alarmsInfoService.getAlarmsInfo(any()) } returns alarmsInfo

        val result = infoAlarmsFeedbackGenerator.generateFeedback(message)

        assertThat(result).isEqualTo(expected)
    }
}
