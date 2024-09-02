// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import com.alliander.sng.Command as ExternalCommand
import com.alliander.sng.CommandStatus as ExternalCommandStatus
import org.apache.avro.specific.SpecificRecordBase
import org.gxf.crestdeviceservice.command.entity.Command as CommandEntity
import org.gxf.crestdeviceservice.command.mapper.CommandFeedbackMapper.commandEntityToCommandFeedback
import org.gxf.crestdeviceservice.command.mapper.CommandFeedbackMapper.externalCommandToCommandFeedback
import org.gxf.crestdeviceservice.config.KafkaProducerProperties
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class CommandFeedbackService(
    private val kafkaTemplate: KafkaTemplate<String, SpecificRecordBase>,
    kafkaProducerProperties: KafkaProducerProperties
) {
    private val topic = kafkaProducerProperties.commandFeedback.topic

    fun sendFeedback(command: ExternalCommand, status: ExternalCommandStatus, message: String) {
        val commandFeedback = externalCommandToCommandFeedback(command, status, message)
        kafkaTemplate.send(topic, command.deviceId, commandFeedback)
    }

    fun sendFeedback(command: CommandEntity, status: ExternalCommandStatus, message: String) {
        val commandFeedback = commandEntityToCommandFeedback(command, status, message)
        kafkaTemplate.send(topic, commandFeedback)
    }
}
