// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.alliander.sng.DeviceCredentials
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.device.entity.Device
import org.gxf.crestdeviceservice.device.service.DeviceService
import org.gxf.crestdeviceservice.psk.service.PskDecryptionService
import org.gxf.crestdeviceservice.psk.service.PskService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class DeviceCredentialsServiceTest {
    @MockK private lateinit var deviceService: DeviceService
    @MockK private lateinit var pskService: PskService
    @MockK private lateinit var pskDecryptionService: PskDecryptionService
    @MockK private lateinit var commandService: CommandService

    @InjectMockKs private lateinit var deviceCredentialsService: DeviceCredentialsService

    @Test
    fun importDeviceCredentialsChangeInitialPsk() {
        val imei = "imei"
        val psk = "encrypted-psk"
        val decryptedPsk = "psk"
        val secret = "encrypted-secret"
        val decryptedSecret = "secret"
        val keyRef = "1"
        val deviceCredentials = DeviceCredentials(imei, psk, secret, keyRef)

        every { pskDecryptionService.decryptSecret(psk, keyRef) } returns decryptedPsk
        every { pskDecryptionService.decryptSecret(secret, keyRef) } returns decryptedSecret
        every { deviceService.createDevice(any(), any()) } answers { Device(arg(0), arg(1)) }
        justRun { pskService.setInitialKeyForDevice(any(), any()) }
        justRun { pskService.generateNewReadyKeyForDevice(any()) }
        every { pskService.changeInitialPsk() } returns true
        every { commandService.saveCommands(any(Command::class), any(Command::class)) } answers
            {
                args as List<Command>
            }

        deviceCredentialsService.importDeviceCredentials(deviceCredentials)

        verify { deviceService.createDevice(imei, decryptedSecret) }
        verify { pskService.setInitialKeyForDevice(imei, decryptedPsk) }
        verify { pskService.generateNewReadyKeyForDevice(imei) }
        verify { commandService.saveCommands(any(), any()) }
    }

    @Test
    fun importDeviceCredentialsWithoutChangingInitialPsk() {
        val imei = "imei"
        val psk = "encrypted-psk"
        val decryptedPsk = "psk"
        val secret = "encrypted-secret"
        val decryptedSecret = "secret"
        val keyRef = "1"
        val deviceCredentials = DeviceCredentials(imei, psk, secret, keyRef)

        every { pskDecryptionService.decryptSecret(psk, keyRef) } returns decryptedPsk
        every { pskDecryptionService.decryptSecret(secret, keyRef) } returns decryptedSecret
        every { deviceService.createDevice(any(), any()) } answers { Device(arg(0), arg(1)) }
        justRun { pskService.setInitialKeyForDevice(any(), any()) }
        every { pskService.changeInitialPsk() } returns false

        deviceCredentialsService.importDeviceCredentials(deviceCredentials)

        verify { deviceService.createDevice(imei, decryptedSecret) }
        verify { pskService.setInitialKeyForDevice(imei, decryptedPsk) }
        verify(exactly = 0) { pskService.generateNewReadyKeyForDevice(imei) }
        verify(exactly = 0) { commandService.saveCommand(any()) }
    }
}
