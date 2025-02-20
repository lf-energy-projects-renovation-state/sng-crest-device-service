// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.model.AlarmsInfo
import org.gxf.crestdeviceservice.model.DeviceMessage
import org.springframework.stereotype.Service

@Service
class AlarmsInfoService {
    private val logger = KotlinLogging.logger {}
    private val mapper = jacksonObjectMapper()
    private val commandRegex = "!?${Command.CommandType.INFO_ALARMS.downlink}".toRegex()

    fun containsResult(message: JsonNode) = DeviceMessage(message).getDownlinkCommand()?.matches(commandRegex) == true

    /** The device answer is logged, no further action is taken */
    fun getAlarmsInfo(message: JsonNode): AlarmsInfo {
        require(containsResult(message))
        val deviceMessage = DeviceMessage(message)
        val alarmThresholds = deviceMessage.getUrcContainingField("AL0")
        logger.debug { "Received alarms info: $alarmThresholds" }
        return mapper.treeToValue<AlarmsInfo>(alarmThresholds)
    }
}
