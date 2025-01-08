// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.resulthandler

import com.fasterxml.jackson.databind.JsonNode
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.springframework.stereotype.Component

@Component
class AnalogAlarmThresholdPort4ResultHandler(
    val commandService: CommandService,
    val commandFeedbackService: CommandFeedbackService,
) : CommandResultHandler(commandService, commandFeedbackService) {
    private val successUrc = "AL7:SET"
    private val errorUrcs = listOf("AL7:DLER", "AL7:ERR")

    override val supportedCommandType = Command.CommandType.ANALOG_ALARM_THRESHOLDS_PORT_4

    override fun hasSucceeded(deviceId: String, body: JsonNode) = successUrc in body.urcs()

    override fun hasFailed(deviceId: String, body: JsonNode) = body.urcs().any { it in errorUrcs }
}
