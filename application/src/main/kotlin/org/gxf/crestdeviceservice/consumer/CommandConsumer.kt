// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.consumer

import com.alliander.sng.CommandStatus
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import com.alliander.sng.Command as ExternalCommand
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class CommandConsumer(
    private val commandService: CommandService,
    private val commandFeedbackService: CommandFeedbackService
) {
    private val logger = KotlinLogging.logger {}

    @KafkaListener(
        id = "command",
        idIsGroup = false,
        topics = ["\${kafka.consumers.command.topic}"])
    fun handleIncomingCommand(command: ExternalCommand) {
        logger.info { "Received command for device: ${command.deviceId}, with correlation id: ${command.correlationId}" }

        // reject command if unknown or if newer same command exists
        val shouldBeRejected = commandService.shouldBeRejected(command)
        if(shouldBeRejected.isPresent) {
            val message = shouldBeRejected.get()
            logger.info { "Rejecting command for device id: ${command.deviceId}, with reason: $message" }
            commandFeedbackService.sendFeedback(command, CommandStatus.Rejected, message)
            return
        }

        // if a same command is already pending, cancel the existing pending command
        val existingPendingCommand = commandService.existingCommandToBeCanceled(command)
        if(existingPendingCommand.isPresent) {
            logger.info { "Device with id ${command.deviceId} already has a pending command of the same type. The existing command will be canceled." }
            val commandToBeCanceled = existingPendingCommand.get()
            commandToBeCanceled.status = Command.CommandStatus.CANCELED
            commandService.saveCommandEntity(commandToBeCanceled)
        }

        commandService.saveExternalCommandAsPending(command)
    }
}
