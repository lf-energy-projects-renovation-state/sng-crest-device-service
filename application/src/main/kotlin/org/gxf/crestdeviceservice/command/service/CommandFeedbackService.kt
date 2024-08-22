// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import com.alliander.sng.CommandFeedback
import com.alliander.sng.CommandStatus
import org.apache.avro.specific.SpecificRecordBase
import org.gxf.crestdeviceservice.config.KafkaProducerProperties
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import com.alliander.sng.Command as ExternalCommand
import org.gxf.crestdeviceservice.command.entity.Command as CommandEntity

@Service
class CommandFeedbackService(
    private val kafkaTemplate: KafkaTemplate<String, SpecificRecordBase>,
    kafkaProducerProperties: KafkaProducerProperties
) {
    private val topic = kafkaProducerProperties.commandFeedback.topic

    fun sendFeedback(command: ExternalCommand, status: CommandStatus, message: String) {
        val commandFeedback =
            CommandFeedback.newBuilder()
                .setDeviceId(command.deviceId)
                .setCorrelationId(command.correlationId)
                .setTimestampStatus(Instant.now())
                .setStatus(status)
                .setMessage(message)
                .build()
        kafkaTemplate.send(topic, commandFeedback)
    }

    fun sendFeedback(command: CommandEntity, status: CommandStatus, message: String) {
        val commandFeedback =
            CommandFeedback.newBuilder()
                .setDeviceId(command.deviceId)
                .setCorrelationId(command.correlationId)
                .setTimestampStatus(Instant.now())
                .setStatus(status)
                .setMessage(message)
                .build()
        kafkaTemplate.send(topic, commandFeedback)
    }
}
