// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.coap

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import org.gxf.crestdeviceservice.psk.PskService
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.util.ResourceUtils

class UrcServiceTest {
    private val pskService = mock<PskService>()
    private val urcService = UrcService(pskService)
    private val mapper = spy<ObjectMapper>()

    companion object {
        private const val URC_FIELD = "URC"
        private const val DL_FIELD = "DL"
    }

    @Test
    fun shouldChangeActiveKeyWhenSuccessURCReceived() {
        val identity = "identity"

        whenever(pskService.needsKeyChange(identity)).thenReturn(false)
        whenever(pskService.isPendingKeyPresent(identity)).thenReturn(true)

        val fileToUse = ResourceUtils.getFile("classpath:messages/message_psk_set_success.json")
        val message = mapper.readTree(fileToUse)

        urcService.interpretURCInMessage(identity, message)

        verify(pskService).changeActiveKey(identity)
    }

    @ParameterizedTest(name = "should set pending key as invalid for {0}")
    @ValueSource(
        strings =
            ["PSK:EQER", "DL:UNK", "[DL]:DLNA", "[DL]:DLER", "[DL]:#ERR", "[DL]:HSER", "[DL]:CSER"])
    fun shouldSetPendingKeyAsInvalidWhenFailureURCReceived(urc: String) {
        val identity = "identity"

        whenever(pskService.needsKeyChange(identity)).thenReturn(false)
        whenever(pskService.isPendingKeyPresent(identity)).thenReturn(true)

        val fileToUse = ResourceUtils.getFile("classpath:messages/message_psk_set_failure.json")
        val messageTemplate = mapper.readTree(fileToUse)
        val receivedCommand =
            "!PSK:umU6KJ4g7Ye5ZU6o:4a3cfdd487298e2f048ebfd703a1da4800c18f2167b62192cf7dc9fd6cc4bcd3;PSK:umU6KJ4g7Ye5ZU6o:4a3cfdd487298e2f048ebfd703a1da4800c18f2167b62192cf7dc9fd6cc4bcd3:SET"
        val message = updatePskCommandInMessage(messageTemplate, urc, receivedCommand)

        urcService.interpretURCInMessage(identity, message)

        verify(pskService).setPendingKeyAsInvalid(identity)
    }

    private fun updatePskCommandInMessage(
        message: JsonNode,
        urc: String,
        receivedCommand: String
    ): JsonNode {
        val newMessage = message as ObjectNode
        val urcList =
            listOf(
                TextNode(urc),
                ObjectNode(JsonNodeFactory.instance, mapOf(DL_FIELD to TextNode(receivedCommand))))
        val urcArray = mapper.valueToTree<ArrayNode>(urcList)
        newMessage.replace(URC_FIELD, urcArray)
        return newMessage
    }
}
