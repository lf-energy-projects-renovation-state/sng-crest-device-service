// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.gxf.crestdeviceservice.Measurement
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class MessageProcessorConsumer {
    private val logger = KotlinLogging.logger {}

    @KafkaListener(topics = ["\${crest-device-service.kafka.topic-name}"], id = "\${crest-device-service.kafka.id}")
    fun consumeMessage(message: ConsumerRecord<String, Measurement>) {
        val jsonNode = ObjectMapper().readTree(message.value().payload)
        logger.info { "Consuming: ${jsonNode.get("ID")}" }
    }
}
