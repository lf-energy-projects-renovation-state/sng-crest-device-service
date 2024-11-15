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

    val confirmationDownlinkInUrc = "CMD:RSP"
    val errorUrc = "RSP:DLER"

    override fun forCommandType() = CommandType.RSP

    override fun hasSucceeded(deviceId: String, body: JsonNode) =
        containsConfirmationDownlinkInUrc(body) && !containsErrorUrc(body)

    override fun hasFailed(deviceId: String, body: JsonNode) = containsErrorUrc(body)

    private fun containsConfirmationDownlinkInUrc(body: JsonNode) =
        getDownlinksFromMessage(body).contains(confirmationDownlinkInUrc)

    private fun containsErrorUrc(body: JsonNode) = getUrcsFromMessage(body).contains(errorUrc)
}
