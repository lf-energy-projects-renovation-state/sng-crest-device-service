// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.resulthandler

import com.fasterxml.jackson.databind.JsonNode
import org.gxf.crestdeviceservice.command.entity.Command.CommandType
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.springframework.stereotype.Component

@Component
class RspCommandResultHandler(commandService: CommandService, commandFeedbackService: CommandFeedbackService) :
    CommandResultHandler(commandService, commandFeedbackService) {

    val successUrc = "RSP:OK"
    val errorUrc = "RSP:DLER"

    override val supportedCommandType = CommandType.RSP

    override fun hasSucceeded(deviceId: String, body: JsonNode) = successUrc in body.urcs()

    override fun hasFailed(deviceId: String, body: JsonNode) = errorUrc in body.urcs()
}
