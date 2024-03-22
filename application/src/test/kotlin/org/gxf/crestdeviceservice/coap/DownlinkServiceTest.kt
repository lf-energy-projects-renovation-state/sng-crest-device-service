package org.gxf.crestdeviceservice.coap

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.TestHelper
import org.gxf.crestdeviceservice.psk.PskService
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class DownlinkServiceTest {

    @Mock
    private lateinit var pskService: PskService

    @InjectMocks
    private lateinit var downLinkService: DownlinkService

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
            PreSharedKeyStatus.READY
        )

        whenever(pskService.needsKeyChange(any())).thenReturn(true)
        whenever(pskService.setReadyKeyForIdentityAsPending(any())).thenReturn(psk)

        val urcNode = TestHelper.unsollicitedResultCodeInit()
        val result = downLinkService.getDownlinkForIdentity(identity, urcNode)

        // Psk command is formatted as: PSK:[Key]:[Hash];PSK:[Key]:[Hash]SET
        println("!PSK:$expectedKey:$expectedHash;PSK:$expectedKey:${expectedHash}SET")
        assertThat(result).isEqualTo("!PSK:$expectedKey:$expectedHash;PSK:$expectedKey:${expectedHash}SET")
    }

    @Test
    fun shouldReturnNoActionDownlinkWhenThereIsNoNewPsk() {
        whenever(pskService.needsKeyChange(any())).thenReturn(false)

        val urcNode = TestHelper.unsollicitedResultCodeInit()
        val result = downLinkService.getDownlinkForIdentity("identity", urcNode)

        assertThat(result).isEqualTo("0")
    }

    @Test
    fun shouldChangeActiveKeyWhenSuccessURCReceived() {
        val urcNode = TestHelper.unsollicitedResultCodeSuccess()
        val identity = "identity"

        whenever(pskService.needsKeyChange(any())).thenReturn(false)
        whenever(pskService.isPendingKeyPresent(any())).thenReturn(true)

        downLinkService.getDownlinkForIdentity(identity, urcNode)

        verify(pskService).changeActiveKey(identity)
    }

    @Test
    fun shouldSetPendingKeyAsInvalidWhenFailureURCReceived() {
        val urcNode = TestHelper.unsollicitedResultCodeFailure()
        val identity = "identity"

        whenever(pskService.needsKeyChange(any())).thenReturn(false)
        whenever(pskService.isPendingKeyPresent(any())).thenReturn(true)

        downLinkService.getDownlinkForIdentity(identity, urcNode)

        verify(pskService).setPendingKeyAsInvalid(identity)
    }
}
