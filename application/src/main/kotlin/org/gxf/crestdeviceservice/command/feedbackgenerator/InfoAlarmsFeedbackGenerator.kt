// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.feedbackgenerator

import com.fasterxml.jackson.databind.JsonNode
import org.gxf.crestdeviceservice.command.entity.Command
import org.springframework.stereotype.Component

@Component
class InfoAlarmsFeedbackGenerator : CommandFeedbackGenerator() {
    override val supportedCommandType = Command.CommandType.INFO_ALARMS

    override fun generateFeedback(body: JsonNode): String {
        TODO("Not yet implemented")
    }
}
