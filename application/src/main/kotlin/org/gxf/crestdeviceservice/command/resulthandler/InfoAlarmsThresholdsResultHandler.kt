// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.resulthandler

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.model.AlarmsInfo
import org.springframework.stereotype.Component

@Component
class InfoAlarmsThresholdsResultHandler(
    commandService: CommandService,
    commandFeedbackService: CommandFeedbackService,
) : CommandResultHandler(commandService, commandFeedbackService) {
    private val mapper = jacksonObjectMapper()
    private val errorUrcs = listOf("INFO:DLER", "INFO:ERR", "INFO:DLNA")

    override val supportedCommandType = Command.CommandType.INFO_ALARMS

    override fun hasSucceeded(command: Command, body: JsonNode) =
        try {
            getAlarmsInfo(body)
            true
        } catch (e: Exception) {
            false
        }

    override fun hasFailed(command: Command, body: JsonNode): Boolean = body.urcs().any { it in errorUrcs }

    override fun handleCommandSpecificSuccess(command: Command, body: JsonNode): String? {
        val alarmsInfo = getAlarmsInfo(body)
        return alarmsInfo.toString()
    }

    private fun getAlarmsInfo(body: JsonNode): AlarmsInfo {
        val downlink = body.downlinks().first { it.contains(supportedCommandType.downlink) }
        val json = downlink.substringAfter(", ")

        return mapper.readValue<AlarmsInfo>(json) // catch exception
    }
}
