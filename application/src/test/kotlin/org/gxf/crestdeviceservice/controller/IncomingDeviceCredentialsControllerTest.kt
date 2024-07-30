// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.controller

import com.alliander.sng.DeviceCredentials
import org.gxf.crestdeviceservice.psk.service.PskDecryptionService
import org.gxf.crestdeviceservice.psk.service.PskService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class IncomingDeviceCredentialsControllerTest {
    private val pskService = mock<PskService>()
    private val pskDecryptionService = mock<PskDecryptionService>()
    private val incomingDeviceCredentialsController =
        IncomingDeviceCredentialsController(pskService, pskDecryptionService)

    @Test
    fun handleIncomingDeviceCredentials() {
        val imei = "imei"
        val psk = "encrypted-psk"
        val decryptedPsk = "psk"
        val secret = "encrypted-secret"
        val decryptedSecret = "secret"
        val keyRef = "1"
        val deviceCredentials = DeviceCredentials(imei, psk, secret, keyRef)

        whenever(pskDecryptionService.decryptSecret(psk, keyRef)).thenReturn(decryptedPsk)
        whenever(pskDecryptionService.decryptSecret(secret, keyRef)).thenReturn(decryptedSecret)
        whenever(pskService.changeInitialPsk()).thenReturn(true)

        incomingDeviceCredentialsController.handleIncomingDeviceCredentials(deviceCredentials)

        verify(pskService).setInitialKeyForIdentity(imei, decryptedPsk, decryptedSecret)
        verify(pskService).generateNewReadyKeyForIdentity(imei)
    }
}
