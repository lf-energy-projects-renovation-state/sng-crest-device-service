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
import org.mockito.kotlin.times
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
        private const val PSK_COMMAND =
            "!PSK:umU6KJ4g7Ye5ZU6o:4a3cfdd487298e2f048ebfd703a1da4800c18f2167b62192cf7dc9fd6cc4bcd3;PSK:umU6KJ4g7Ye5ZU6o:4a3cfdd487298e2f048ebfd703a1da4800c18f2167b62192cf7dc9fd6cc4bcd3:SET"
        private const val IDENTITY = "identity"
    }

    @Test
    fun shouldChangeActiveKeyWhenSuccessURCReceived() {
        val urc = "PSK:SET"
        interpretURCWhileNewKeyIsPending(urc)
        verify(pskService).changeActiveKey(IDENTITY)
    }

    @ParameterizedTest(name = "should set pending key as invalid for {0}")
    @ValueSource(
        strings =
            ["PSK:EQER", "DL:UNK", "[DL]:DLNA", "[DL]:DLER", "[DL]:#ERR", "[DL]:HSER", "[DL]:CSER"])
    fun shouldSetPendingKeyAsInvalidWhenFailureURCReceived(urc: String) {
        interpretURCWhileNewKeyIsPending(urc)
        verify(pskService).setPendingKeyAsInvalid(IDENTITY)
    }

    @ParameterizedTest(name = "should not set pending key as invalid for {0}")
    @ValueSource(
        strings =
            ["INIT", "ENPD", "TEL:RBT", "PSK:TMP", "JTR", "WDR", "BOR", "EXR", "POR", "TS:ERR"])
    fun shouldNotSetPendingKeyAsInvalidWhenOtherURCReceived(urc: String) {
        interpretURCWhileNewKeyIsPending(urc)
        verify(pskService, times(0)).setPendingKeyAsInvalid(IDENTITY)
    }

    private fun interpretURCWhileNewKeyIsPending(urc: String) {
        whenever(pskService.needsKeyChange(IDENTITY)).thenReturn(false)
        whenever(pskService.isPendingKeyPresent(IDENTITY)).thenReturn(true)

        val message = updatePskCommandInMessage(urc)

        urcService.interpretURCInMessage(IDENTITY, message)
    }

    private fun updatePskCommandInMessage(urc: String): JsonNode {
        val message = messageTemplate()
        val urcList =
            listOf(
                TextNode(urc),
                ObjectNode(JsonNodeFactory.instance, mapOf(DL_FIELD to TextNode(PSK_COMMAND))))
        val urcArray = mapper.valueToTree<ArrayNode>(urcList)
        message.replace(URC_FIELD, urcArray)
        return message
    }

    private fun messageTemplate(): ObjectNode {
        val fileToUse = ResourceUtils.getFile("classpath:message-template.json")
        return mapper.readTree(fileToUse) as ObjectNode
    }
}
