// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.resulthandler

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.MessageFactory
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class InfoAlarmsThresholdsResultHandlerTest {
    @MockK private lateinit var commandService: CommandService
    @MockK private lateinit var commandFeedbackService: CommandFeedbackService

    @InjectMockKs private lateinit var resultHandler: InfoAlarmsThresholdsResultHandler

    val command = CommandFactory.infoAlarmsCommandInProgress()
    val urcs = listOf<String>()
    val downlink = "\"!INFO:ALARMS\", {\"AL0\":[0,1,0,1,0], \"AL1\":[0,0,0,0,0], \"AL6\":[100,200,300,400,10]}"
    val message = MessageFactory.messageWithUrc(urcs, downlink)

    @Test
    fun hasSucceeded() {
        val result = resultHandler.hasSucceeded(command, message)

        assertThat(result).isTrue()
    }

    @Test
    fun handleCommandSpecificSuccess() {
        val result = resultHandler.handleCommandSpecificSuccess(command, message)

        assertThat(result!!.contains("AL6"))
    }
}
