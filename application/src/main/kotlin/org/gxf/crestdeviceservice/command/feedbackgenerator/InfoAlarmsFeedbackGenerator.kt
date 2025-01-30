// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.feedbackgenerator

import com.fasterxml.jackson.databind.JsonNode
import kotlin.reflect.full.memberProperties
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.mapper.AnalogAlarmsThresholdTranslator
import org.gxf.crestdeviceservice.command.service.AlarmsInfoService
import org.gxf.crestdeviceservice.model.AlarmsInfo
import org.springframework.stereotype.Component

@Component
class InfoAlarmsFeedbackGenerator(private val alarmsInfoService: AlarmsInfoService) : CommandFeedbackGenerator {
    override val supportedCommandType = Command.CommandType.INFO_ALARMS

    override fun generateFeedback(message: JsonNode): String {
        val alarmsInfo = alarmsInfoService.getAlarmsInfo(message)
        return printAlarmsInfo(alarmsInfo)
    }

    private fun printAlarmsInfo(alarmsInfo: AlarmsInfo) =
        """{${
            AlarmsInfo::class.memberProperties
                .filter { it.get(alarmsInfo) != null }
                .map { printThresholdsForOneChannel(it.name, it.get(alarmsInfo)) }
                .joinToString(", ") { it }
        }}"""

    private fun printThresholdsForOneChannel(channel: String, thresholds: Any?) =
        "\"${AnalogAlarmsThresholdTranslator.translateFromChannel(channel)}\":$thresholds"
}
