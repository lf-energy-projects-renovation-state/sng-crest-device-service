// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.consumer

import com.alliander.sng.Command as ExternalCommand
import com.alliander.sng.CommandStatus
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.psk.service.PskService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class CommandConsumer(
    private val commandService: CommandService,
    private val commandFeedbackService: CommandFeedbackService,
    private val pskService: PskService
) {
    private val logger = KotlinLogging.logger {}

    @KafkaListener(
        id = "command", idIsGroup = false, topics = ["\${kafka.consumers.command.topic}"])
    fun handleIncomingCommand(command: ExternalCommand) {
        logger.info {
            "Received command ${command.command} for device: ${command.deviceId}, with correlation id: ${command.correlationId}"
        }

        // reject command if unknown or if newer same command exists
        val reasonForRejection = commandService.shouldBeRejected(command)
        if (reasonForRejection.isPresent) {
            logger.warn {
                "Command ${command.command} for device ${command.deviceId} is rejected. Reason: $reasonForRejection"
            }
            sendRejectionFeedback(reasonForRejection.get(), command)
            return
        }

        commandFeedbackService.sendFeedback(command, CommandStatus.Received, "Command received")

        // if a same command is already pending, cancel the existing pending command
        val commandToBeCanceled = commandService.existingCommandToBeCanceled(command)
        if (commandToBeCanceled != null) {
            cancelExistingCommand(command, commandToBeCanceled)
        }

        if (isPskCommand(command)) {
            pskService.generateNewReadyKeyForDevice(command.deviceId)
        }

        commandService.saveExternalCommandAsPending(command)
    }

    private fun sendRejectionFeedback(reason: String, command: ExternalCommand) {
        logger.info { "Rejecting command for device id: ${command.deviceId}, with reason: $reason" }
        commandFeedbackService.sendFeedback(command, CommandStatus.Rejected, reason)
    }

    private fun isPskCommand(command: com.alliander.sng.Command) =
        command.command.lowercase().contains("psk")

    private fun cancelExistingCommand(command: ExternalCommand, commandToBeCanceled: Command) {
        logger.warn {
            "Device ${command.deviceId} already has a pending command of type ${commandToBeCanceled.type}. The first command will be canceled."
        }
        val message =
            "Command canceled by newer same command with correlation id: ${command.correlationId}"
        commandFeedbackService.sendFeedback(commandToBeCanceled, CommandStatus.Cancelled, message)
        commandService.saveCommandWithNewStatus(commandToBeCanceled, Command.CommandStatus.CANCELED)
    }
}
