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
    fun getDownlinkForDevice(deviceId: String, body: JsonNode): String {
        logger.debug { "Check if device $deviceId needs key change" }
        if (pskService.needsKeyChange(deviceId)) {
            logger.info { "Device $deviceId needs key change" }
            val newKey = pskService.setReadyKeyForIdentityAsPending(deviceId)

            // After setting a new psk, the device will send a new message if the psk set was
            // successful
            logger.debug {
                "Create PSK set command for key for device ${newKey.identity} with revision ${newKey.revision} and status ${newKey.status}"
            }
            return createPskSetCommand(newKey)
        }

        val pendingCommand = commandService.getFirstPendingCommandForDevice(deviceId)
        val commandInProgress = commandService.getFirstCommandInProgressForDevice(deviceId)
        if (pendingCommand != null && commandInProgress == null) { // no other commands in progress
            logger.info { "Device $deviceId has pending command of type: ${pendingCommand.type}" }
            val commandToSend = commandService.saveCommandWithNewStatus(pendingCommand, Command.CommandStatus.IN_PROGRESS)

            return createDownlinkCommand(commandToSend)
        }

        return RESPONSE_SUCCESS
    }

    fun createPskSetCommand(newPreSharedKey: PreSharedKey): String {
        val newKey = newPreSharedKey.preSharedKey
        val hash = DigestUtils.sha256Hex("${newPreSharedKey.secret}${newKey}")
        return "!PSK:${newKey}:${hash};PSK:${newKey}:${hash}:SET"
    }

    private fun createDownlinkCommand(command: Command) =
        when (command.type) {
            CommandType.REBOOT -> "!CMD:REBOOT"
        }
}
