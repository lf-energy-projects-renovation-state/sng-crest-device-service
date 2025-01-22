// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.exception.NoCommandResultHandlerForCommandTypeException
import org.gxf.crestdeviceservice.command.resulthandler.CommandResultHandler
import org.springframework.stereotype.Service

@Service
class CommandResultService(
    private val commandService: CommandService,
    private val commandResultHandlersByType: Map<Command.CommandType, CommandResultHandler>,
) {
    private val logger = KotlinLogging.logger {}

    fun handleMessage(deviceId: String, body: JsonNode) {
        val commandsInProgress = commandService.getAllCommandsInProgressForDevice(deviceId)

        commandsInProgress.forEach { checkResult(it, body) }
    }

    private fun checkResult(command: Command, body: JsonNode) {
        logger.debug { "Checking result for pending command of type ${command.type} for device ${command.deviceId}" }

        val resultHandler =
            commandResultHandlersByType[command.type]
                ?: throw NoCommandResultHandlerForCommandTypeException(
                    "No command result handler for command type ${command.type}"
                )

        when {
            resultHandler.hasSucceeded(command, body) -> resultHandler.handleSuccess(command)
            resultHandler.hasFailed(command, body) -> resultHandler.handleFailure(command, body)
            else -> resultHandler.handleStillInProgress(command)
        }
    }
}
