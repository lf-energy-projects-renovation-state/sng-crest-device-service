// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.apache.commons.codec.digest.DigestUtils
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.entity.Command.CommandType
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
    fun getDownlinkForIdentity(identity: String, body: JsonNode): String {
        logger.debug { "Check if device $identity needs key change" }
        if (pskService.needsKeyChange(identity)) {
            logger.info { "Device $identity needs key change" }
            val newKey = pskService.setReadyKeyForIdentityAsPending(identity)

            // After setting a new psk, the device will send a new message if the psk set was
            // successful
            logger.debug {
                "Create PSK set command for key for device ${newKey.identity} with revision ${newKey.revision} and status ${newKey.status}"
            }
            return createPskSetCommand(newKey)
        }

        val pendingCommands = commandService.getPendingCommandsForDevice(identity)
        if (pendingCommands.isNotEmpty()) {
            pendingCommands.forEach{ command -> run {
                logger.info { "Device $identity has pending command of type: ${command.type}" }
                commandService.setCommandInProgress(command)
            } }

            return createDownlinkCommands(pendingCommands)
        }

        return RESPONSE_SUCCESS
    }

    fun createPskSetCommand(newPreSharedKey: PreSharedKey): String {
        val newKey = newPreSharedKey.preSharedKey
        val hash = DigestUtils.sha256Hex("${newPreSharedKey.secret}${newKey}")
        return "!PSK:${newKey}:${hash};PSK:${newKey}:${hash}:SET"
    }

    fun createDownlinkCommands(commands: List<Command>) =
        commands.joinToString(";") { command -> createDownlinkCommand(command) }

    fun createDownlinkCommand(command: Command) =
        when (command.type) {
            CommandType.REBOOT -> "CMD:REBOOT"
            CommandType.FIRMWARE -> "OTA" // todo FDP-1620 create downlink for firmware updates
        }
}
