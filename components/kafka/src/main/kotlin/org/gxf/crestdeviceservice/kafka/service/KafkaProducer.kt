// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.kafka.service

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import org.gxf.crestdeviceservice.kafka.port.MessageProducer
import org.slf4j.Logger
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service


@Service
class KafkaProducer(
        private val kafkaTemplate: KafkaTemplate<String, String>,
) : MessageProducer<JsonNode> {
    private val logger: Logger = KotlinLogging.logger {}

    override fun produceMessage(message: JsonNode) {
        logger.info("Producing: ${message.get("ID")}")
        kafkaTemplate.send("topic", message.toString())
    }
}
