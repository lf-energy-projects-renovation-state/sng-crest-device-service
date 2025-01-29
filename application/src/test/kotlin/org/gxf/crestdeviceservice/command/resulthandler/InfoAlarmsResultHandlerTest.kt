// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.resulthandler

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import java.io.IOException
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.MessageFactory
import org.gxf.crestdeviceservice.TestConstants.ALARMS_INFO_DOWNLINK
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.feedbackgenerator.InfoAlarmsFeedbackGenerator
import org.gxf.crestdeviceservice.command.service.AlarmsInfoService
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.model.AlarmsInfo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class InfoAlarmsResultHandlerTest {
    @MockK private lateinit var infoAlarmsFeedbackGenerator: InfoAlarmsFeedbackGenerator

    @MockK private lateinit var commandService: CommandService
    @MockK private lateinit var commandFeedbackService: CommandFeedbackService
    @MockK private lateinit var alarmsInfoService: AlarmsInfoService

    @InjectMockKs private lateinit var resultHandler: InfoAlarmsResultHandler

    val command = CommandFactory.infoAlarmsCommandInProgress()
    private val urcs = listOf<String>()
    val downlink = ALARMS_INFO_DOWNLINK
    val message = MessageFactory.messageWithUrc(urcs, downlink)

    @Test
    fun hasSucceeded() {
        every { alarmsInfoService.getAlarmsInfo(any()) } returns AlarmsInfo()

        val result = resultHandler.hasSucceeded(command, message)

        assertThat(result).isTrue()
    }

    @Test
    fun hasSucceededFails() {
        every { alarmsInfoService.getAlarmsInfo(any()) } throws (IOException())

        val result = resultHandler.hasSucceeded(command, message)

        assertThat(result).isFalse()
    }

    @Test
    fun handleSuccess() {
        val command = CommandFactory.infoAlarmsCommandInProgress()
        val message = MessageFactory.messageWithUrc(listOf(), ALARMS_INFO_DOWNLINK)
        val generatedFeedback = "feedback"
        every { commandService.saveCommand(any()) } answers { firstArg() }
        justRun { commandFeedbackService.sendSuccessFeedback(any(), any()) }
        every { infoAlarmsFeedbackGenerator.generateFeedback(any()) } answers { generatedFeedback }

        resultHandler.handleSuccess(command, message, infoAlarmsFeedbackGenerator)

        assertThat(command.status).isEqualTo(Command.CommandStatus.SUCCESSFUL)
        verify { commandService.saveCommand(command) }
        verify { commandFeedbackService.sendSuccessFeedback(command, generatedFeedback) }
    }
}
