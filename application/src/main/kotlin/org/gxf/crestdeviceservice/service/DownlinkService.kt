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
        val pendingCommands = commandService.getAllPendingCommandsForDevice(deviceId)
        val commandsToSend = commandsToSend(pendingCommands)
        if(commandsToSend.isNotEmpty()) {
            return getDownlinkFromCommands(deviceId, commandsToSend)
        }
        return RESPONSE_SUCCESS
    }

    fun commandsToSend(pendingCommands: List<Command>) =
        pendingCommands.filter { command -> commandCanBeSent(command) }

    // if command is psk set and pending psk exists in repository
    fun commandCanBeSent(command: Command) =
        command.type != Command.CommandType.PSK_SET || pskService.readyForPskSetCommand(command.deviceId)

    private fun getDownlinkFromCommands(deviceId: String, pendingCommands: List<Command>): String {
        val types = pendingCommands.joinToString(", ") { command -> command.type.toString() }
        logger.info { "Device $deviceId has pending commands of types: $types. These commands will be sent to the device." }

        return pendingCommands
            .filter { command -> fitsInMaxMessageSize(command) }
            .map { command ->
                commandService.saveCommandWithNewStatus(command, Command.CommandStatus.IN_PROGRESS)
            }.joinToString(";") { command -> getDownlinkPerCommand(command) }
    }

    private fun fitsInMaxMessageSize(command: Command) = true // todo

    private fun getDownlinkPerCommand(command: Command): String {
        if(command.type == Command.CommandType.PSK) {
            val newKey = pskService.getCurrentReadyPsk(command.deviceId)
                ?: throw NoExistingPskException("There is no new key ready to be set")
            return createPskCommand(newKey)
        }
        if(command.type == Command.CommandType.PSK_SET) {
            val newKey = preparePskChange(command.deviceId)
            logger.debug {
                "Create PSK set command for key for device ${newKey.identity} with revision ${newKey.revision} and status ${newKey.status}"
            }
            return createPskSetCommand(newKey)
        }

        return "!${command.type.downlink}"
    }

    private fun preparePskChange(deviceId: String): PreSharedKey {
        logger.info { "Device $deviceId needs key change" }
        return pskService.setPskToPendingForDevice(deviceId)
    }

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
