// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.kafka.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.observation.annotation.Observed
import mu.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.gxf.crestdeviceservice.kafka.port.MessageConsumer
import org.slf4j.Logger
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Service
import java.net.SocketTimeoutException

@Service
class KafkaConsumer : MessageConsumer<ConsumerRecord<String, String>> {
    private val logger: Logger = KotlinLogging.logger {}

    @Observed(name = "consumer.consumed")
    @KafkaListener(topics = ["topic"], id = "gxf-kafka-consumer")
    @RetryableTopic(
            backoff = Backoff(value = 3000L),
            attempts = "2",
            include = [SocketTimeoutException::class]
    )
    override fun consumeMessage(message: ConsumerRecord<String, String>) {
        val jsonNode = ObjectMapper().readTree(message.value())
        logger.info("Consuming: ${jsonNode.get("ID")}")
    }
}
