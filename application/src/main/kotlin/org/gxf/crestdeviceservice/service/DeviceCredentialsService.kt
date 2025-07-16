// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.alliander.sng.DeviceCredentials
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.device.service.DeviceService
import org.gxf.crestdeviceservice.psk.service.PskDecryptionService
import org.gxf.crestdeviceservice.psk.service.PskService
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class DeviceCredentialsService(
    private val deviceService: DeviceService,
    private val pskService: PskService,
    private val pskDecryptionService: PskDecryptionService,
    private val commandService: CommandService,
) {
    private val logger = KotlinLogging.logger {}

    fun importDeviceCredentials(deviceCredentials: DeviceCredentials) {
        logger.info { "Received key for ${deviceCredentials.imei}" }

        val deviceId = deviceCredentials.imei

        val decryptedPsk = pskDecryptionService.decryptSecret(deviceCredentials.psk, deviceCredentials.keyRef)
        val decryptedSecret = pskDecryptionService.decryptSecret(deviceCredentials.secret, deviceCredentials.keyRef)

        deviceService.createDevice(deviceId, decryptedSecret)
        pskService.setInitialKeyForDevice(deviceId, decryptedPsk)

        if (pskService.changeInitialPsk()) {
            pskService.generateNewReadyKeyForDevice(deviceId)
            preparePskCommands(deviceId)
        }
    }

    private fun preparePskCommands(deviceId: String) {
        logger.info { "Prepare pending PSK and PSK_SET commands for PSK change for device $deviceId." }
        val pskCommand =
            Command(
                id = UUID.randomUUID(),
                deviceId = deviceId,
                correlationId = UUID.randomUUID(),
                timestampIssued = Instant.now(),
                type = Command.CommandType.PSK,
                status = Command.CommandStatus.PENDING,
                commandValue = null,
            )
        val pskSetCommand =
            Command(
                id = UUID.randomUUID(),
                deviceId = deviceId,
                correlationId = UUID.randomUUID(),
                timestampIssued = Instant.now(),
                type = Command.CommandType.PSK_SET,
                status = Command.CommandStatus.PENDING,
                commandValue = null,
            )

        commandService.saveCommands(pskCommand, pskSetCommand)
    }
}
