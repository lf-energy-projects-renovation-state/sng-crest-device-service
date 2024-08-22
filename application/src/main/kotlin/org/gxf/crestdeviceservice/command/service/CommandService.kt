package org.gxf.crestdeviceservice.command.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.entity.Command.CommandStatus
import org.gxf.crestdeviceservice.command.repository.CommandRepository
import org.gxf.crestdeviceservice.model.ErrorUrc.Companion.isErrorUrc
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Optional
import java.util.UUID
import com.alliander.sng.Command as ExternalCommand
import com.alliander.sng.CommandStatus as ExternalCommandStatus

@Service
class CommandService(
    private val commandRepository: CommandRepository,
    private val commandFeedbackService: CommandFeedbackService
) {
    private val knownCommands = Command.CommandType.entries.map { it.name }
    private val logger = KotlinLogging.logger {}

    companion object {
        const val INITIALISATION = "INIT"
    }

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

        return deviceHasSameCommandAlreadyPendingOrInProgress(command.deviceId, commandType)
    }

    /**
     * Check if the device already has a newer command pending of the same type that was issued at a later date.
     * This check prevents issues if commands arrive out of order or if we reset the kafka consumer group.
     */
    private fun deviceHasNewerSameCommand(
        deviceId: String,
        commandType: Command.CommandType,
        timestampNewCommand: Instant
    ): Boolean {
        val latestCommandInDatabase = latestCommandInDatabase(deviceId, commandType)
            ?: return false

        // If the device already has a newer command in the database
        return latestCommandInDatabase.timestampIssued.isAfter(timestampNewCommand)
    }

    private fun latestCommandInDatabase(deviceId: String, commandType: Command.CommandType) =
        commandRepository.findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(deviceId, commandType)

    /**
     * Check if the device already has a newer command pending of the same type that was issued at a later date.
     * This check prevents issues if commands arrive out of order or if we reset the kafka consumer group.
     */
    private fun deviceHasSameCommandAlreadyPendingOrInProgress(
        deviceId: String,
        commandType: Command.CommandType
    ): Optional<Command> {
        val latestCommandInDatabase = latestCommandInDatabase(deviceId, commandType)
            ?: return Optional.empty()

        // The latest command is pending or in progress
        if (latestCommandInDatabase.status == CommandStatus.PENDING || latestCommandInDatabase.status == CommandStatus.IN_PROGRESS) {
            return Optional.of(latestCommandInDatabase)
        }

        return Optional.empty()
    }

    fun getFirstPendingCommandForDevice(deviceId: String) =
        commandRepository.findFirstByDeviceIdAndStatusOrderByTimestampIssuedAsc(deviceId, CommandStatus.PENDING)

    fun getFirstCommandInProgressForDevice(deviceId: String) =
        commandRepository.findFirstByDeviceIdAndStatusOrderByTimestampIssuedAsc(deviceId, CommandStatus.IN_PROGRESS)

    fun saveExternalCommandAsPending(incomingCommand: ExternalCommand) {
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

    fun setCommandInProgress(command: Command) { // todo equivalent voor canceled?
        command.status = CommandStatus.IN_PROGRESS
        commandRepository.save(command)
    }

    fun handleCommandError(deviceId: String, command: Command, urcs: List<String>) {
        val errorUrcs = urcs.filter { urc -> isErrorUrc(urc) }
        val message = "Command failed for device with id $deviceId with code(s): ${errorUrcs.joinToString { ", " }}"

        logger.error { message }

        command.status = CommandStatus.ERROR
        saveCommandEntity(command)

        commandFeedbackService.sendFeedback(command, ExternalCommandStatus.Error, message)
    }

    fun handleCommandUrcs(deviceId: String, command: Command, urcs: List<String>) {
        when(command.type) {
            Command.CommandType.REBOOT -> handleRebootUrcs(deviceId, command, urcs)
        }
    }

    private fun handleRebootUrcs(deviceId: String, command: Command, urcs: List<String>) {
        if(urcs.contains(INITIALISATION)) {
            val message = "Reboot for device $deviceId went succesfully"
            logger.info { message }
            command.status = CommandStatus.SUCCESSFUL
            saveCommandEntity(command)

            commandFeedbackService.sendFeedback(command, ExternalCommandStatus.Successful, message)
        } else {
            logger.warn { "Reboot command sent for device $deviceId, did not receive expected urc: $INITIALISATION. Urcs received: ${urcs.joinToString { ", " }}" }
        }
    }
}
