// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import com.alliander.sng.Command as ExternalCommand
import java.time.Instant
import java.util.Optional
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.entity.Command.CommandStatus
import org.gxf.crestdeviceservice.command.mapper.CommandMapper.externalCommandToCommandEntity
import org.gxf.crestdeviceservice.command.mapper.CommandMapper.translateCommand
import org.gxf.crestdeviceservice.command.repository.CommandRepository
import org.springframework.stereotype.Service

@Service
class CommandService(private val commandRepository: CommandRepository) {
    private val knownCommands = Command.CommandType.entries.map { it.name }

    /**
     * Check if the incoming command should be rejected.
     *
     * @return An optional string with the reason for rejection, or empty if the command should be
     *   accepted.
     */
    fun shouldBeRejected(command: ExternalCommand): Optional<String> {
        val translatedCommand = translateCommand(command.command)
        if (translatedCommand !in knownCommands) {
            return Optional.of("Unknown command")
        }

        val deviceId = command.deviceId
        val commandType: Command.CommandType = Command.CommandType.valueOf(translatedCommand)
        if (deviceHasNewerSameCommand(deviceId, commandType, command.timestamp)) {
            return Optional.of("There is a newer command of the same type")
        }

        if(deviceHasSameCommandAlreadyInProgress(deviceId, commandType)) {
            return Optional.of("A command of the same type is already in progress.")
        }

        return Optional.empty()
    }

    fun existingCommandToBeCanceled(command: ExternalCommand): Command? {
        val translatedCommand = translateCommand(command.command)
        val commandType: Command.CommandType = Command.CommandType.valueOf(translatedCommand)

        return sameCommandForDeviceAlreadyPending(command.deviceId, commandType)
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
    ) = commandRepository
        .findAllByDeviceIdAndTypeAndStatusOrderByTimestampIssuedAsc(deviceId, commandType, CommandStatus.IN_PROGRESS)
        .isNotEmpty()

    private fun latestCommandInDatabase(deviceId: String, commandType: Command.CommandType) =
        commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(
            deviceId, commandType)

    private fun sameCommandForDeviceAlreadyPending(
        deviceId: String,
        commandType: Command.CommandType
    ): Command? {
        val latestCommandInDatabase =
            latestCommandInDatabase(deviceId, commandType) ?: return null

        // The latest command is pending
        if (latestCommandInDatabase.status == CommandStatus.PENDING) {
            return latestCommandInDatabase
        }

        return null
    }

    fun getFirstPendingCommandForDevice(deviceId: String) =
        commandRepository.findFirstByDeviceIdAndStatusOrderByTimestampIssuedAsc(
            deviceId, CommandStatus.PENDING)

    fun getFirstCommandInProgressForDevice(deviceId: String) =
        commandRepository.findFirstByDeviceIdAndStatusOrderByTimestampIssuedAsc(
            deviceId, CommandStatus.IN_PROGRESS)

    fun getAllPendingCommandsForDevice(deviceId: String) =
        commandRepository.findAllByDeviceIdAndStatusOrderByTimestampIssuedAsc(
            deviceId, CommandStatus.PENDING)

    fun getAllCommandsInProgressForDevice(deviceId: String) =
        commandRepository.findAllByDeviceIdAndStatusOrderByTimestampIssuedAsc(deviceId, CommandStatus.IN_PROGRESS)

    fun saveExternalCommandAsPending(incomingCommand: ExternalCommand) {
        val commandEntity =
            externalCommandToCommandEntity(incomingCommand, CommandStatus.PENDING)

        commandRepository.save(commandEntity)
    }

    fun saveCommandEntity(command: Command) = commandRepository.save(command)

    fun saveCommandWithNewStatus(command: Command, status: CommandStatus): Command {
        command.status = status
        return commandRepository.save(command)
    }
}
