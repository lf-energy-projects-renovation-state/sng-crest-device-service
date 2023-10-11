// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.service

import com.fasterxml.jackson.databind.JsonNode
import org.gxf.crestdeviceservice.kafka.KafkaProducer
import org.springframework.stereotype.Service

@Service
class MessageService(private val messageProducer: KafkaProducer) {
    fun handleMessage(message: JsonNode) {
        messageProducer.produceMessage(message)
    }
}