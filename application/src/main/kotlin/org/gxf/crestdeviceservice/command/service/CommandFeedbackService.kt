package org.gxf.crestdeviceservice.command.service

import com.alliander.sng.Command as ExternalCommand
import com.alliander.sng.CommandFeedback
import com.alliander.sng.CommandStatus
import org.gxf.crestdeviceservice.command.entity.Command as CommandEntity
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class CommandFeedbackService(
    val kafkaTemplate: KafkaTemplate<String, CommandFeedback>,
    @Value("\${kafka.producers.command-feedback.topic}")
    val commandFeedbackTopic: String
) {
    fun sendFeedback(command: ExternalCommand, status: CommandStatus, message: String) {
        val commandFeedback = CommandFeedback.newBuilder()
            .setDeviceId(command.deviceId)
            .setCorrelationId(command.correlationId)
            .setTimestampStatus(Instant.now())
            .setStatus(status)
            .setMessage(message)
            .build()
        kafkaTemplate.send(commandFeedbackTopic, commandFeedback)
    }

    fun sendFeedback(command: CommandEntity, status: CommandStatus, message: String) {
        val commandFeedback = CommandFeedback.newBuilder()
            .setDeviceId(command.deviceId)
            .setCorrelationId(command.correlationId)
            .setTimestampStatus(Instant.now())
            .setStatus(status)
            .setMessage(message)
            .build()
        kafkaTemplate.send(commandFeedbackTopic, commandFeedback)
    }
}
