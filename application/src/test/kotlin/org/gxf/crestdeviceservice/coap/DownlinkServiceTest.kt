// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.coap

import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.TestHelper
import org.gxf.crestdeviceservice.psk.PskService
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DownlinkServiceTest {
    private val pskService = mock<PskService>()
    private val downLinkService = DownlinkService(pskService)
    private val message = TestHelper.messageTemplate()

    companion object {
        private const val IDENTITY = "867787050253370"
    }

    @Test
    fun shouldReturnPskDownlinkWhenThereIsANewPsk() {
        val expectedKey = "key"
        val expectedHash = "238104b039438f9dcbbef1dd6e295aa3cf2f248406c01ba7f6034becfe1a53d9"
        val psk =
            PreSharedKey(
                IDENTITY, 1, Instant.now(), expectedKey, "secret", PreSharedKeyStatus.PENDING)

        whenever(pskService.getCurrentActiveKey(IDENTITY)).thenReturn("oldKey")
        whenever(pskService.needsKeyChange(IDENTITY)).thenReturn(true)
        whenever(pskService.setReadyKeyForIdentityAsPending(IDENTITY)).thenReturn(psk)

        val result = downLinkService.getDownlinkForIdentity(IDENTITY, message)

        // Psk command is formatted as: PSK:[Key]:[Hash];PSK:[Key]:[Hash]:SET
        assertThat(result)
            .isEqualTo("!PSK:${expectedKey}:${expectedHash};PSK:${expectedKey}:${expectedHash}:SET")
    }

    @Test
    fun shouldReturnNoActionDownlinkWhenThereIsNoNewPsk() {
        whenever(pskService.needsKeyChange(IDENTITY)).thenReturn(false)

        val result = downLinkService.getDownlinkForIdentity(IDENTITY, message)

        assertThat(result).isEqualTo("0")
    }
}
