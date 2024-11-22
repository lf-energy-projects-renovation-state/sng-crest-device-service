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
class RebootCommandResultHandler(commandService: CommandService, commandFeedbackService: CommandFeedbackService) :
    CommandResultHandler(commandService, commandFeedbackService) {

    private val succesUrcs = listOf("INIT", "WDR")

    override val supportedCommandType = CommandType.REBOOT

    override fun hasSucceeded(deviceId: String, body: JsonNode) = body.urcs().containsAll(succesUrcs)

    override fun hasFailed(deviceId: String, body: JsonNode) = false
}
