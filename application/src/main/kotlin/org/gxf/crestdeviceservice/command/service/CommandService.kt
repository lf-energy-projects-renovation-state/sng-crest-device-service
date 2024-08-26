// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import com.alliander.sng.Command as ExternalCommand
import java.time.Instant
import java.util.Optional
import java.util.UUID
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.entity.Command.CommandStatus
import org.gxf.crestdeviceservice.command.repository.CommandRepository
import org.springframework.stereotype.Service

@Service
class CommandService(private val commandRepository: CommandRepository) {
    private val knownCommands = Command.CommandType.entries.map { it.name }

    companion object {
        const val INITIALISATION = "INIT"
    }

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

        val commandType: Command.CommandType = Command.CommandType.valueOf(translatedCommand)
        if (deviceHasNewerSameCommand(command.deviceId, commandType, command.timestamp)) {
            return Optional.of("Device has a newer command of the same type")
        }

        return Optional.empty()
    }

    fun existingCommandToBeCanceled(command: ExternalCommand): Optional<Command> {
        val translatedCommand = translateCommand(command.command)
        val commandType: Command.CommandType = Command.CommandType.valueOf(translatedCommand)

        return deviceHasSameCommandAlreadyPendingOrInProgress(command.deviceId, commandType)
    }

    private fun translateCommand(command: String) = // todo add to enum
    command.trim('!').uppercase()

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

    private fun latestCommandInDatabase(deviceId: String, commandType: Command.CommandType) =
        commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(
            deviceId, commandType)

    /**
     * Check if the device already has a newer command pending of the same type that was issued at a
     * later date. This check prevents issues if commands arrive out of order or if we reset the
     * kafka consumer group.
     */
    private fun deviceHasSameCommandAlreadyPendingOrInProgress(
        deviceId: String,
        commandType: Command.CommandType
    ): Optional<Command> {
        val latestCommandInDatabase =
            latestCommandInDatabase(deviceId, commandType) ?: return Optional.empty()

        // The latest command is pending or in progress
        if (latestCommandInDatabase.status == CommandStatus.PENDING ||
            latestCommandInDatabase.status == CommandStatus.IN_PROGRESS) {
            return Optional.of(latestCommandInDatabase)
        }

        return Optional.empty()
    }

    fun getFirstPendingCommandForDevice(deviceId: String) =
        commandRepository.findFirstByDeviceIdAndStatusOrderByTimestampIssuedAsc(
            deviceId, CommandStatus.PENDING)

    fun getFirstCommandInProgressForDevice(deviceId: String) =
        commandRepository.findFirstByDeviceIdAndStatusOrderByTimestampIssuedAsc(
            deviceId, CommandStatus.IN_PROGRESS)

    fun saveExternalCommandAsPending(incomingCommand: ExternalCommand) {
        val commandEntity =
            Command(
                id = UUID.randomUUID(),
                deviceId = incomingCommand.deviceId,
                correlationId = incomingCommand.correlationId,
                timestampIssued = incomingCommand.timestamp,
                type = Command.CommandType.valueOf(translateCommand(incomingCommand.command)),
                status = CommandStatus.PENDING,
                commandValue = incomingCommand.value,
            )

        commandRepository.save(commandEntity)
    }

    fun saveCommandEntity(command: Command) = commandRepository.save(command)

    fun setCommandInProgress(command: Command) { // todo equivalent voor canceled?
        command.status = CommandStatus.IN_PROGRESS
        commandRepository.save(command)
    }
}
