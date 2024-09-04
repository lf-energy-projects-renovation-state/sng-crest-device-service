// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import com.alliander.sng.CommandFeedback
import org.apache.avro.specific.SpecificRecordBase
import org.gxf.crestdeviceservice.config.KafkaProducerProperties
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class CommandFeedbackService(
    private val kafkaTemplate: KafkaTemplate<String, SpecificRecordBase>,
    kafkaProducerProperties: KafkaProducerProperties
) {
    private val topic = kafkaProducerProperties.commandFeedback.topic

    fun sendFeedback(commandFeedback: CommandFeedback) {
        kafkaTemplate.send(topic, commandFeedback.deviceId, commandFeedback)
    }
}
