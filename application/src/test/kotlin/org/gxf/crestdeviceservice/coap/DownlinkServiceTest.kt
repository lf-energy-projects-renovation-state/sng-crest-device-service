// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.coap

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.psk.PskService
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.util.ResourceUtils
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class DownlinkServiceTest {

    @Mock
    private lateinit var pskService: PskService

    @InjectMocks
    private lateinit var downLinkService: DownlinkService

    private val mapper = ObjectMapper()

    @Test
    fun shouldReturnPskDownlinkWhenThereIsANewPsk() {
        val identity = "identity"
        val expectedKey = "key"
        val expectedHash = "ad165b11320bc91501ab08613cc3a48a62a6caca4d5c8b14ca82cc313b3b96cd"
        val psk = PreSharedKey(
            identity,
            1,
            Instant.now(),
            expectedKey,
            "secret",
            PreSharedKeyStatus.PENDING
        )

        whenever(pskService.needsKeyChange(identity)).thenReturn(true)
        whenever(pskService.setReadyKeyForIdentityAsPending(identity)).thenReturn(psk)

        val fileToUse = ResourceUtils.getFile("classpath:messages/message.json")
        val message = mapper.readTree(fileToUse)

        val result = downLinkService.getDownlinkForIdentity(identity, message)

        // Psk command is formatted as: PSK:[Key]:[Hash];PSK:[Key]:[Hash]SET
        assertThat(result).isEqualTo("!PSK:$expectedKey:$expectedHash;PSK:$expectedKey:${expectedHash}SET")
    }

    @Test
    fun shouldReturnNoActionDownlinkWhenThereIsNoNewPsk() {
        val identity = "identity"
        whenever(pskService.needsKeyChange(identity)).thenReturn(false)

        val fileToUse = ResourceUtils.getFile("classpath:messages/message.json")
        val message = mapper.readTree(fileToUse)

        val result = downLinkService.getDownlinkForIdentity(identity, message)

        assertThat(result).isEqualTo("0")
    }

    @Test
    fun shouldChangeActiveKeyWhenSuccessURCReceived() {
        val identity = "identity"

        whenever(pskService.needsKeyChange(identity)).thenReturn(false)
        whenever(pskService.isPendingKeyPresent(identity)).thenReturn(true)

        val fileToUse = ResourceUtils.getFile("classpath:messages/message_psk_set_success.json")
        val message = mapper.readTree(fileToUse)

        downLinkService.getDownlinkForIdentity(identity, message)

        verify(pskService).changeActiveKey(identity)
    }

    @Test
    fun shouldSetPendingKeyAsInvalidWhenFailureURCReceived() {
        val identity = "identity"

        whenever(pskService.needsKeyChange(identity)).thenReturn(false)
        whenever(pskService.isPendingKeyPresent(identity)).thenReturn(true)

        val fileToUse = ResourceUtils.getFile("classpath:messages/message_psk_set_failure.json")
        val message = mapper.readTree(fileToUse)
        downLinkService.getDownlinkForIdentity(identity, message)

        verify(pskService).setPendingKeyAsInvalid(identity)
    }
}
