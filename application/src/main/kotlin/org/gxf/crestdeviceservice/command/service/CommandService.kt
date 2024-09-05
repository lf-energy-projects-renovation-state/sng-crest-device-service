// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.exception.CommandValidationException
import org.gxf.crestdeviceservice.command.repository.CommandRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class CommandService(
    private val commandRepository: CommandRepository,
    private val commandFeedbackService: CommandFeedbackService
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Validate the Command.
     * @throws CommandValidationException if validation fails
     */
    fun validate(command: Command) {
        if (deviceHasNewerSameCommand(command.deviceId, command.type, command.timestampIssued)) {
            throw CommandValidationException("There is a newer command of the same type")
        }

        if (deviceHasSameCommandAlreadyInProgress(command.deviceId, command.type)) {
            throw CommandValidationException("A command of the same type is already in progress.")
        }
    }

    /**
     * Check if the device already has a newer command pending of the same type that was issued at a
     * later date. This check prevents issues if commands arrive out of order or if we reset the
     * kafka consumer group.
     */
    private fun deviceHasNewerSameCommand(
        deviceId: String,
        commandType: Command.CommandType,
        timestampNewCommand: Instant
    ): Boolean {
        val latestCommandInDatabase = latestCommandInDatabase(deviceId, commandType) ?: return false

        // If the device already has a newer command in the database
        return latestCommandInDatabase.timestampIssued.isAfter(timestampNewCommand)
    }

    private fun deviceHasSameCommandAlreadyInProgress(
        deviceId: String,
        commandType: Command.CommandType
    ) =
        commandRepository
            .findAllByDeviceIdAndTypeAndStatusOrderByTimestampIssuedAsc(
                deviceId, commandType, Command.CommandStatus.IN_PROGRESS)
            .isNotEmpty()

    private fun latestCommandInDatabase(deviceId: String, commandType: Command.CommandType) =
        commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(
            deviceId, commandType)

    fun cancelOlderCommandIfNecessary(pendingCommand: Command) {
        sameCommandForDeviceAlreadyPending(pendingCommand)
            ?.let { commandToBeCancelled ->
                {
                    logger.warn {
                        "Device ${commandToBeCancelled.deviceId} already has a pending command of type ${commandToBeCancelled.type}. The first command will be cancelled."
                    }
                    cancelExistingCommand(
                        pendingCommand.correlationId,
                        commandToBeCancelled
                    )
                }
            }
    }

    fun sameCommandForDeviceAlreadyPending(
        command: Command
    ): Command? {
        val latestCommandInDatabase = latestCommandInDatabase(command.deviceId, command.type) ?: return null

        if (latestCommandInDatabase.status == Command.CommandStatus.PENDING) {
            return latestCommandInDatabase
        }

        return null
    }

    fun cancelExistingCommand(newCorrelationId: UUID, commandToBeCancelled: Command) {
        val message =
            "Command cancelled by newer same command with correlation id: $newCorrelationId"
        commandFeedbackService.sendCancellationFeedback(commandToBeCancelled, message)
        saveCommandWithNewStatus(commandToBeCancelled, Command.CommandStatus.CANCELLED)
    }

    fun isPskCommand(command: Command) =
        command.type == Command.CommandType.PSK || command.type == Command.CommandType.PSK_SET

    fun getFirstCommandInProgressForDevice(deviceId: String) =
        commandRepository.findFirstByDeviceIdAndStatusOrderByTimestampIssuedAsc(
            deviceId, Command.CommandStatus.IN_PROGRESS)

    fun getAllPendingCommandsForDevice(deviceId: String) =
        commandRepository.findAllByDeviceIdAndStatusOrderByTimestampIssuedAsc(
            deviceId, Command.CommandStatus.PENDING)

    fun getAllCommandsInProgressForDevice(deviceId: String) =
        commandRepository.findAllByDeviceIdAndStatusOrderByTimestampIssuedAsc(
            deviceId, Command.CommandStatus.IN_PROGRESS)

    fun save(command: Command) {
        commandRepository.save(command)
    }

    fun saveCommandEntities(commands: List<Command>): MutableIterable<Command> =
        commandRepository.saveAll(commands)

    fun saveCommandWithNewStatus(command: Command, status: Command.CommandStatus): Command {
        command.status = status
        return commandRepository.save(command)
    }
}
