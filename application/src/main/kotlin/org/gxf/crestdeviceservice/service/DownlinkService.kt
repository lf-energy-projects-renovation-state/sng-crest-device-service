// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.fasterxml.jackson.databind.JsonNode
import com.github.fzakaria.ascii85.Ascii85
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.apache.commons.codec.digest.DigestUtils
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.config.MessageProperties
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

    // if command is psk set and pending psk exists in repository
    private fun commandCanBeSent(command: Command) =
        command.type != Command.CommandType.PSK_SET ||
            pskService.readyForPskSetCommand(command.deviceId)

    private fun getDownlinkFromCommands(deviceId: String, pendingCommands: List<Command>): String {
        val types = pendingCommands.joinToString(", ") { command -> command.type.toString() }
        logger.info {
            "Device $deviceId has pending commands of types: $types. These commands will be sent to the device."
        }

        val downlink = pendingCommands
            .filter { command -> fitsInMaxMessageSize(getDownlinkPerCommand(command)) }
            .map { command ->
                commandService.saveCommandWithNewStatus(command, Command.CommandStatus.IN_PROGRESS)
            }
            .joinToString(";") { command -> getDownlinkPerCommand(command) }
        downlinkCumulative = ""
        return downlink
    }

    fun fitsInMaxMessageSize(downlinkToAdd: String): Boolean {
        val currentSize = Ascii85.encode(downlinkCumulative.toByteArray()).toByteArray().size

        val newCumulative = if(downlinkCumulative.isEmpty()) {
            downlinkToAdd
        } else {
            downlinkCumulative.plus(";$downlinkToAdd")
        }
        val newSize = Ascii85.encode(newCumulative.toByteArray()).toByteArray().size
        logger.debug {
            "Trying to add a downlink '$downlinkToAdd' to existing downlink '$downlinkCumulative'. " +
                    "Current downlink size: $currentSize. Downlink size after after adding: $newSize."
        }
        if(newSize <= messageProperties.maxBytes) {
            downlinkCumulative = newCumulative
            return true
        }
        return false
    }

    private fun getDownlinkPerCommand(command: Command): String {
        if (command.type == Command.CommandType.PSK) {
            val newKey =
                pskService.getCurrentReadyPsk(command.deviceId)
                    ?: throw NoExistingPskException("There is no new key ready to be set")
            return createPskCommand(newKey)
        }
        if (command.type == Command.CommandType.PSK_SET) {
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
