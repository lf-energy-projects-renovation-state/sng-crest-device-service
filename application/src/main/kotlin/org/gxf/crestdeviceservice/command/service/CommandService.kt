package org.gxf.crestdeviceservice.command.service

import com.alliander.sng.Command as ExternalCommand
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.entity.Command.CommandStatus
import org.gxf.crestdeviceservice.command.repository.CommandRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class CommandService(
    private val commandRepository: CommandRepository
) {
    private val knownCommands = Command.CommandType.entries.map { it.name }

    /**
     * Check if the incoming command should be rejected.
     *
     * @return An optional string with the reason for rejection, or empty if the command should be accepted.
     */
    fun shouldBeRejected(command: ExternalCommand): Optional<String> {
        if (command.command !in knownCommands) {
            return Optional.of("Unknown command")
        }

        val commandType: Command.CommandType = Command.CommandType.valueOf(command.command)
        if(deviceHasNewerSameCommand(command.deviceId, commandType, command.timestamp)) {
            return Optional.of("Device has a newer command of the same type")
        }

        return Optional.empty()
    }

    fun existingCommandToBeCanceled(command: ExternalCommand): Optional<Command> {
        val commandType: Command.CommandType = Command.CommandType.valueOf(command.command)

        return deviceHasSameCommandAlreadyPendingOrInProgress(command.deviceId, commandType, command.timestamp)
    }

    /**
     * Check if the device already has a newer command pending of the same type that was issued at a later date.
     * This check prevents issues if commands arrive out of order or if we reset the kafka consumer group.
     */
    fun deviceHasNewerSameCommand(
        deviceId: String,
        commandType: Command.CommandType,
        timestampNewCommand: Instant
    ): Boolean {
        val latestCommandInDatabase = latestCommandInDatabase(deviceId, commandType)
            ?: return false

        // If the device already has a newer command in the database
        return latestCommandInDatabase.timestampIssued.isAfter(timestampNewCommand)
    }

    fun latestCommandInDatabase(deviceId: String, commandType: Command.CommandType) =
        commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(deviceId, commandType)

    /**
     * Check if the device already has a newer command pending of the same type that was issued at a later date.
     * This check prevents issues if commands arrive out of order or if we reset the kafka consumer group.
     */
    fun deviceHasSameCommandAlreadyPendingOrInProgress(
        deviceId: String,
        commandType: Command.CommandType,
        timestampNewCommand: Instant
    ): Optional<Command> {
        val latestCommandInDatabase = latestCommandInDatabase(deviceId, commandType)
            ?: return Optional.empty()

        // The latest command is pending or in progress
        if (latestCommandInDatabase.status == CommandStatus.PENDING || latestCommandInDatabase.status == CommandStatus.IN_PROGRESS) {
            return Optional.of(latestCommandInDatabase)
        }

        return Optional.empty()
    }

    fun getPendingCommandsForDevice(deviceId: String) =
        commandRepository.findByDeviceIdAndStatusOrderByTimestampIssuedAsc(deviceId, CommandStatus.PENDING)

    fun saveExternalCommand(incomingCommand: ExternalCommand) {

        val commandEntity = Command(
            id = UUID.randomUUID(),
            deviceId = incomingCommand.deviceId,
            correlationId = incomingCommand.correlationId,
            timestampIssued = incomingCommand.timestamp,
            type = Command.CommandType.valueOf(incomingCommand.command),
            status = CommandStatus.PENDING,
            commandValue = incomingCommand.value,
        )

        commandRepository.save(commandEntity)
    }

    fun saveCommandEntity(command: Command) =
        commandRepository.save(command)

    fun setCommandInProgress(command: Command) {
        command.status = CommandStatus.IN_PROGRESS
        commandRepository.save(command)
    }

    fun handleCommandError(identity: String, pendingCommands: List<Command>) {
        TODO("Not yet implemented")
    }

    fun handleCommandSuccess(identity: String, pendingCommands: List<Command>) {
        TODO("Not yet implemented")
    }
}
