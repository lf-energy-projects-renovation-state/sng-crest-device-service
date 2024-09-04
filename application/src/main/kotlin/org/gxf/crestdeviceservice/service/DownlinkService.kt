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
import org.gxf.crestdeviceservice.config.MessageProperties
import org.gxf.crestdeviceservice.model.Downlink
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.exception.NoExistingPskException
import org.gxf.crestdeviceservice.psk.service.PskService
import org.springframework.stereotype.Service

@Service
class DownlinkService(
    private val pskService: PskService,
    private val commandService: CommandService,
    private val messageProperties: MessageProperties
) {
    companion object {
        private const val RESPONSE_SUCCESS = "0"
    }

    private val logger = KotlinLogging.logger {}
    var downlinkCumulative = ""

    @Transactional
    @Throws(NoExistingPskException::class)
    fun getDownlinkForDevice(deviceId: String, body: JsonNode): String {
        val pendingCommands = commandService.getAllPendingCommandsForDevice(deviceId)
        val commandsToSend = commandsToSend(pendingCommands)
        if (commandsToSend.isNotEmpty()) {
            return getDownlinkFromCommands(deviceId, commandsToSend)
        }
        return RESPONSE_SUCCESS
    }

    private fun commandsToSend(pendingCommands: List<Command>) =
        pendingCommands.filter { command -> commandCanBeSent(command) }

    // if command is psk_set and pending psk exists in repository
    private fun commandCanBeSent(command: Command) =
        command.type != Command.CommandType.PSK_SET ||
            pskService.readyForPskSetCommand(command.deviceId)

    private fun getDownlinkFromCommands(deviceId: String, pendingCommands: List<Command>): String {
        logger.info {
            "Device $deviceId has pending commands of types: ${printableCommandTypes(pendingCommands)}."
        }

        val commandsToSend =
            pendingCommands.filter { command ->
                fitsInMaxMessageSize(getDownlinkPerCommand(command))
            }
        downlinkCumulative = ""

        logger.info { "Commands that will be sent: ${printableCommandTypes(commandsToSend)}." }

        val downlink =
            commandsToSend.joinToString(";") { command -> getDownlinkPerCommand(command) }

        commandsToSend.forEach { command -> setCommandInProgress(command) }

        logger.debug { "Downlink that will be sent: $downlink" }
        return downlink
    }

    private fun printableCommandTypes(commands: List<Command>) =
        commands.joinToString(", ") { command -> command.type.toString() }

    private fun setCommandInProgress(command: Command) {
        if (command.type == Command.CommandType.PSK_SET) {
            val deviceId = command.deviceId
            logger.info { "Device $deviceId needs key change" }
            pskService.setPskToPendingForDevice(deviceId)
        }
        commandService.saveCommandWithNewStatus(command, Command.CommandStatus.IN_PROGRESS)
    }

    fun fitsInMaxMessageSize(downlinkToAdd: String): Boolean {
        val currentSize = downlinkCumulative.length

        val newCumulative =
            if (downlinkCumulative.isEmpty()) {
                downlinkToAdd
            } else {
                downlinkCumulative.plus(";$downlinkToAdd")
            }
        val newSize = newCumulative.length
        logger.debug {
            "Trying to add a downlink '$downlinkToAdd' to existing downlink '$downlinkCumulative'. " +
                "Current downlink size: $currentSize. Downlink size after after adding: $newSize."
        }
        if (newSize <= messageProperties.maxBytes) {
            downlinkCumulative = newCumulative
            return true
        }
        return false
    }

    private fun getDownlinkPerCommand(command: Command): String {
        if (command.type == Command.CommandType.PSK) {
            val newKey = getCurrentReadyPsk(command)

            return createPskCommand(newKey)
        }
        if (command.type == Command.CommandType.PSK_SET) {
            val newKey = getCurrentReadyPsk(command)
            logger.debug {
                "Create PSK set command for key for device ${newKey.identity} with revision ${newKey.revision} and status ${newKey.status}"
            }
            return createPskSetCommand(newKey)
        }

        return "!${command.type.downlink}"
    }

    private fun getCurrentReadyPsk(command: Command) =
        pskService.getCurrentReadyPsk(command.deviceId)
            ?: throw NoExistingPskException("There is no new key ready to be set")

    fun createPskCommand(newPreSharedKey: PreSharedKey): String {
        val newKey = newPreSharedKey.preSharedKey
        val hash = DigestUtils.sha256Hex("${newPreSharedKey.secret}${newKey}")
        return "!PSK:${newKey}:${hash}"
    }

    fun createPskSetCommand(newPreSharedKey: PreSharedKey): String {
        val newKey = newPreSharedKey.preSharedKey
        val hash = DigestUtils.sha256Hex("${newPreSharedKey.secret}${newKey}")
        return "PSK:${newKey}:${hash}:SET"
    }
}
