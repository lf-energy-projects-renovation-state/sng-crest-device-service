// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.ShipmentFileFactory
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import java.io.File

class ShipmentFileServiceTest {
    private val deviceCredentialsServiceMock = mockk<DeviceCredentialsService>()

    @Test
    fun mapShipmentFileToDTO() {
        val shipmentFileService = ShipmentFileService(deviceCredentialsServiceMock)
        val testFile = File("src/test/resources/shipmentFile.json")
        val multipartFile = MockMultipartFile(testFile.name, testFile.name, null, testFile.readBytes())

        val mappedFile = shipmentFileService.mapShipmentFileToDTO(multipartFile)
        assertThat(mappedFile).isEqualTo(ShipmentFileFactory.testShipmentFileDTO())
    }

    @Test
    fun mapDTOToProducerRecords() {
        val shipmentFileService = ShipmentFileService(deviceCredentialsServiceMock)
        val testDevice = ShipmentFileFactory.testDevice()
        val credentials = shipmentFileService.mapDTOToDeviceCredentials(ShipmentFileFactory.testShipmentFileDTO())

        assertThat(credentials.size).isEqualTo(1)

        val credential = credentials[0]

        assertThat(credential.imei).isEqualTo(testDevice.rtu.rtuId)
        assertThat(credential.psk).isEqualTo(testDevice.rtu.pskEncrypted)
        assertThat(credential.secret).isEqualTo(testDevice.rtu.pskChangeSecretEncrypted)
        assertThat(credential.keyRef).isEqualTo(testDevice.rtu.pskEncryptionKeyRef)
    }
}
