// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.feedbackgenerator

import com.fasterxml.jackson.databind.JsonNode
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.AlarmsInfoService
import org.springframework.stereotype.Component

@Component
class InfoAlarmsFeedbackGenerator(private val alarmsInfoService: AlarmsInfoService) : CommandFeedbackGenerator {
    override val supportedCommandType = Command.CommandType.INFO_ALARMS

    override fun generateFeedback(body: JsonNode) = alarmsInfoService.getAlarmsInfo(body).toString()
}
