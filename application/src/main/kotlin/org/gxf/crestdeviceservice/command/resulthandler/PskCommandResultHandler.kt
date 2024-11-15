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
class PskCommandResultHandler(commandService: CommandService, commandFeedbackService: CommandFeedbackService) :
    CommandResultHandler(commandService, commandFeedbackService) {

    private val succesUrc = "PSK:TMP"
    private val errorUrcs = listOf("PSK:DLER", "PSK:HSER")

    override fun forCommandType() = CommandType.PSK

    override fun hasSucceeded(deviceId: String, body: JsonNode) = containsSuccessUrc(body)

    override fun hasFailed(deviceId: String, body: JsonNode) = containsErrorUrc(body)

    private fun containsSuccessUrc(body: JsonNode) = getUrcsFromMessage(body).contains(succesUrc)

    private fun containsErrorUrc(body: JsonNode) = getUrcsFromMessage(body).any { it in errorUrcs }
}
