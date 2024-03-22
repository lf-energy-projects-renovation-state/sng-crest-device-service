package org.gxf.crestdeviceservice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

object TestHelper {
    // Returns a JsonNode representation of: ["INIT", {"DL": "0"}]
    fun unsollicitedResultCodeInit(): ArrayNode {
        val mapper = ObjectMapper()
        val urcList = listOf(
            TextNode("INIT"),
            ObjectNode(JsonNodeFactory.instance, mapOf("DL" to TextNode("0")))
        )
        return mapper.valueToTree(urcList)
    }

    fun unsollicitedResultCodeSuccess(): ArrayNode {
        val mapper = ObjectMapper()
        val urcList = listOf(
            TextNode("PSK:SET"),
            ObjectNode(
                JsonNodeFactory.instance,
                mapOf("DL" to TextNode("!PSK:key:ad165b11320bc91501ab08613cc3a48a62a6caca4d5c8b14ca82cc313b3b96cd;PSK:key:ad165b11320bc91501ab08613cc3a48a62a6caca4d5c8b14ca82cc313b3b96cdSET"))
            )
        )
        return mapper.valueToTree(urcList)
    }

    fun unsollicitedResultCodeFailure(): ArrayNode {
        val mapper = ObjectMapper()
        val urcList = listOf(
            TextNode("PSK:EQER"),
            ObjectNode(
                JsonNodeFactory.instance,
                mapOf("DL" to TextNode("!PSK:key:ad165b11320bc91501ab08613cc3a48a62a6caca4d5c8b14ca82cc313b3b96cd;PSK:key:ad165b11320bc91501ab08613cc3a48a62a6caca4d5c8b14ca82cc313b3b96cdSET"))
            )
        )
        return mapper.valueToTree(urcList)
    }
}
