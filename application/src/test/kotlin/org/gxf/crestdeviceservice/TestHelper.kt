package org.gxf.crestdeviceservice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

object TestHelper {
    // Returns a JsonNode representation of: ["INIT", {"DL": "0"}]
    fun unsollicitedResultCode(): ArrayNode {
        val mapper = ObjectMapper()
        val urcList = listOf(
            TextNode("INIT"),
            ObjectNode(JsonNodeFactory.instance, mapOf("DL" to TextNode("0")))
        )
        return mapper.valueToTree(urcList)
    }
}
