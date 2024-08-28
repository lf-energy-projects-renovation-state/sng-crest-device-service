// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.alliander.sng.CommandStatus as ExternalCommandStatus
import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.entity.Command.CommandStatus
import org.gxf.crestdeviceservice.command.service.CommandFeedbackService
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.model.ErrorUrc.Companion.isErrorUrc
import org.gxf.crestdeviceservice.model.ErrorUrc.Companion.isPskErrorUrc
import org.gxf.crestdeviceservice.model.ErrorUrc.Companion.messageFromCode
import org.gxf.crestdeviceservice.psk.exception.NoExistingPskException
import org.gxf.crestdeviceservice.psk.service.PskService
import org.springframework.stereotype.Service

@Service
class UrcService(
    private val pskService: PskService,
    private val commandService: CommandService,
    private val commandFeedbackService: CommandFeedbackService
) {
    companion object {
        private const val URC_FIELD = "URC"
        private const val DL_FIELD = "DL"
    }

    private val logger = KotlinLogging.logger {}

    fun interpretURCInMessage(deviceId: String, body: JsonNode) {
        val urcs = getUrcsFromMessage(body)
        if (urcs.isEmpty()) {
            logger.debug { "Received message without urcs" }
        } else {
            logger.debug { "Received message with urcs ${urcs.joinToString(", ")}" }
        }

        val downlinkFromMessage = getDownlinkFromMessage(body)
        val commandInProgress = commandService.getFirstCommandInProgressForDevice(deviceId)

        if (commandInProgress != null && downlinkConcernsCommandInProgress(downlinkFromMessage, commandInProgress)) {
            handleUrcsForCommand(urcs, commandInProgress)
        } else {
            logger.warn { "Device message dl field does not contain the command sent in the previous downlink." +
                    "Command in progress: ${commandInProgress?.type}. " +
                    "Received urcs: ${urcs.joinToString(", ")}," +
                    "relating to downlink: $downlinkFromMessage." }
        }
    }

    private fun downlinkConcernsCommandInProgress(
        downlink: String,
        commandInProgress: Command
    ) = downlink.contains(commandInProgress.type.downlink)

    private fun getUrcsFromMessage(body: JsonNode) =
        body[URC_FIELD].filter { it.isTextual }.map { it.asText() }

    private fun getDownlinkFromMessage(body: JsonNode) =
        body[URC_FIELD].first { it.isObject }[DL_FIELD].asText()

    private fun handleUrcsForCommand(
        urcs: List<String>,
        commandInProgress: Command
    ) {
        if (urcsContainErrors(urcs)) {
            handleCommandError(commandInProgress, urcs)
        } else if (urcsContainSuccessesForCommand(urcs, commandInProgress)) {
            handleCommandSuccess(commandInProgress)
        } else {
            logger.warn { "Unexpected urcs received for command ${commandInProgress.type}. Urcs received: ${urcs.joinToString(", ")}." }
        }
    }

    private fun urcsContainErrors(urcs: List<String>) = urcs.any { urc -> isErrorUrc(urc) }

    private fun urcsContainSuccessesForCommand(urcs: List<String>, command: Command) =
        command.type.urcsSuccess.any { successUrc -> urcs.contains(successUrc) }

    private fun urcsContainPskErrors(urcs: List<String>) = urcs.any { urc -> isPskErrorUrc(urc) }

    private fun handleCommandError(command: Command, urcs: List<String>) {
        if(command.type == Command.CommandType.PSK) {
            handlePskErrors(urcs, command)
        }
        val errorMessages = urcs.joinToString(". ") { urc -> messageFromCode(urc) }

        val message =
            "Command ${command.type} failed for device with id ${command.deviceId}. Result code(s): $errorMessages."

        logger.error { message }

        val commandWithErrorStatus =
            commandService.saveCommandWithNewStatus(command, CommandStatus.ERROR)

        commandFeedbackService.sendFeedback(
            commandWithErrorStatus, ExternalCommandStatus.Error, message)
    }

    private fun handlePskErrors(urcs: List<String>, command: Command) {
        val deviceId = command.deviceId
        if(!urcsContainPskErrors(urcs)) {
            logger.warn {
                "Psk set command sent for device $deviceId. Urcs received do not seem to be about psk: ${urcs.joinToString { ", " }}."
            }
        }
        if (!pskService.isPendingPskPresent(deviceId)) {
            throw NoExistingPskException(
                "Failure URC received, but no pending key present to set as invalid"
            )
        }
        pskService.setPendingKeyAsInvalid(deviceId)
    }

    private fun handleCommandSuccess(command: Command) {
        if(command.type == Command.CommandType.PSK) {
            handlePskSetSuccess(command)
        }
        logger.info { "Command ${command.type} for device ${command.deviceId} handled successfully. Saving command and sending feedback to Maki." }

        val successfulCommand =
            commandService.saveCommandWithNewStatus(command, CommandStatus.SUCCESSFUL)

        commandFeedbackService.sendFeedback(
            successfulCommand, ExternalCommandStatus.Successful, "Command handled successfully")
    }

    private fun handlePskSetSuccess(command: Command) {
        val deviceId = command.deviceId
            if (!pskService.isPendingPskPresent(deviceId)) {
                throw NoExistingPskException(
                    "Success URC received, but no pending key present to set as active"
                )
            }
            logger.info { "PSK set successfully, changing active key" }
            pskService.changeActiveKey(deviceId)
    }
}
