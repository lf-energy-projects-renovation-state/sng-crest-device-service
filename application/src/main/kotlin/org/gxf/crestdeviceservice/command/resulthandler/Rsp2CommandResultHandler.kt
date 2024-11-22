// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.resulthandler

import org.gxf.crestdeviceservice.command.entity.Command.CommandType
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.springframework.stereotype.Component

@Component
class Rsp2CommandResultHandler(commandService: CommandService, commandFeedbackService: CommandFeedbackService) :
    RspCommandResultHandler(commandService, commandFeedbackService) {

    override val confirmationDownlinkInUrc = "CMD:RSP2"
    override val errorUrc = "RSP2:DLER"

    override val supportedCommandType = CommandType.RSP2
}
