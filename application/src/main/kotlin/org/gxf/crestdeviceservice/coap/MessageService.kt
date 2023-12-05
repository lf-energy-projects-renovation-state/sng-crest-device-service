// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.coap

import com.fasterxml.jackson.databind.JsonNode
import org.gxf.crestdeviceservice.kafka.MeasurementProducer
import org.springframework.stereotype.Service

@Service
class MessageService(private val messageProducer: MeasurementProducer) {
    fun handleMessage(message: JsonNode) {
        messageProducer.produceMessage(message)
    }
}
