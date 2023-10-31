// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.kafka

import mu.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class ConfigurationConsumer {
    private val logger = KotlinLogging.logger {}

    @KafkaListener(topics = ["\${crest-device-service.kafka.configuration-consumer.topic-name}"], id = "\${crest-device-service.kafka.configuration-consumer.id}")
    fun consumeMessage(message: ConsumerRecord<String, String>) {
        logger.info { "Consuming maki message" }
    }
}
