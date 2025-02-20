// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.util.ResourceUtils

object MessageFactory {
    private const val URC_FIELD = "URC"
    private const val DL_FIELD = "DL"

    private val mapper = jacksonObjectMapper()

    fun messageTemplate(): ObjectNode {
        val messageFile = ResourceUtils.getFile("classpath:message-template.json")
        return mapper.readTree(messageFile) as ObjectNode
    }

    fun messageWithUrc(urcs: List<String> = listOf(), downlink: String = ""): JsonNode {
        val downlinkString = """{"$DL_FIELD":"$downlink"}"""
        val urcObjects =
            (urcs + downlinkString).map { if (isJsonObject(it)) mapper.readTree(it) else mapper.readValue(""""$it"""") }
        val arrayNode = mapper.createArrayNode().addAll(urcObjects)

        val message = messageTemplate()
        message.replace(URC_FIELD, arrayNode)

        return message
    }

    private fun isJsonObject(it: String) = it.startsWith("{") || it.startsWith("[")
}
