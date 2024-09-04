// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import java.time.Instant
import java.util.UUID
import org.mockito.kotlin.spy
import org.springframework.util.ResourceUtils

object TestHelper {
    private val mapper = spy<ObjectMapper>()
    const val DEVICE_ID = "device-id"
    const val MESSAGE_RECEIVED = "Command received"
    val CORRELATION_ID = UUID.randomUUID()
    val timestamp = Instant.now()

    fun messageTemplate(): ObjectNode {
        val messageFile = ResourceUtils.getFile("classpath:message-template.json")
        return mapper.readTree(messageFile) as ObjectNode
    }
}
