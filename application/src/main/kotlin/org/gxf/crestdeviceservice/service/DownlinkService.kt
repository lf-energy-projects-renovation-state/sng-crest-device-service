// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.apache.commons.codec.digest.DigestUtils
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.exception.NoExistingPskException
import org.gxf.crestdeviceservice.psk.service.PskService
import org.springframework.stereotype.Service

@Service
class DownlinkService(
    private val pskService: PskService,
    private val commandService: CommandService
) {
    companion object {
        private const val RESPONSE_SUCCESS = "0"
    }

    private val logger = KotlinLogging.logger {}

    @Transactional
    @Throws(NoExistingPskException::class)
    fun getDownlinkForDevice(deviceId: String, body: JsonNode): String {
        val pendingCommandToSend = pendingCommandToSend(deviceId)
        if(pendingCommandToSend != null && commandCanBeSent(pendingCommandToSend)) {
            return getCommandDownlink(pendingCommandToSend)
        }
        return RESPONSE_SUCCESS
    }

    private fun pendingCommandToSend(deviceId: String): Command? {
        val commandInProgress = commandService.getFirstCommandInProgressForDevice(deviceId)

        // if there is no other command already in progress
        if(commandInProgress == null) {
            val pendingCommand = commandService.getFirstPendingCommandForDevice(deviceId)
            return pendingCommand
        }
        return null
    }

    // if command is psk set and pending psk exists in repository
    fun commandCanBeSent(command: Command) =
        command.type != Command.CommandType.PSK || pskService.readyForPskSetCommand(command.deviceId)

    private fun getCommandDownlink(pendingCommand: Command): String {
        logger.info { "Device ${pendingCommand.deviceId} has pending command of type: ${pendingCommand.type}. This command will be sent to the device." }

        val commandToSend =
            commandService.saveCommandWithNewStatus(
                pendingCommand, Command.CommandStatus.IN_PROGRESS)

        if(pendingCommand.type == Command.CommandType.PSK) {
            return preparePskChange(pendingCommand.deviceId)
        }

        return commandToSend.type.downlink
    }

    private fun preparePskChange(deviceId: String): String {
        logger.info { "Device $deviceId needs key change" }
        val newKey = pskService.setPskToPendingForDevice(deviceId)

        // After setting a new psk, the device will send a new message if the psk set was
        // successful
        logger.debug {
            "Create PSK set command for key for device ${newKey.identity} with revision ${newKey.revision} and status ${newKey.status}"
        }
        return createPskSetCommand(newKey)
    }

    fun createPskSetCommand(newPreSharedKey: PreSharedKey): String {
        val newKey = newPreSharedKey.preSharedKey
        val hash = DigestUtils.sha256Hex("${newPreSharedKey.secret}${newKey}")
        return "!PSK:${newKey}:${hash};PSK:${newKey}:${hash}:SET"
    }
}
