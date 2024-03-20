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
import org.mockito.kotlin.whenever
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class DownlinkServiceTest {

    @Mock
    private lateinit var pskService: PskService

    @InjectMocks
    private lateinit var downLinkService: DownlinkService

//    @Test
//    fun shouldReturnNoActionDownlinkWhenThereIsNoNewPsk() {
//        whenever(pskService.needsKeyChange(any())).thenReturn(false)
//        whenever(pskService.getCurrentPskWithStatus(any(), any())).thenReturn()
//
//        val urcNode = TestHelper.unsollicitedResultCode()
//        val result = downLinkService.getDownlinkForIdentity("identity", urcNode)
//
//        assertThat(result).isEqualTo("0")
//    }

    @Test
    fun shouldReturnPskDownlinkWhenThereIsANewPsk() {
        val expectedKey = "key"
        val expectedHash = "ad165b11320bc91501ab08613cc3a48a62a6caca4d5c8b14ca82cc313b3b96cd"

        whenever(pskService.needsKeyChange(any())).thenReturn(true)
        whenever(pskService.generateAndSetNewKeyForIdentity(any())).thenReturn(
            PreSharedKey(
                "identity",
                0,
                Instant.now(),
                expectedKey,
                "secret",
                PreSharedKeyStatus.PENDING
            )
        )

        val urcNode = TestHelper.unsollicitedResultCode()
        val result = downLinkService.getDownlinkForIdentity("identity", urcNode)

        // Psk command is formatted as: PSK:[Key]:[Hash];PSK:[Key]:[Hash]SET
        assertThat(result).isEqualTo("!PSK:$expectedKey:$expectedHash;PSK:$expectedKey:${expectedHash}SET")
    }

    @Test
    fun shouldInterpretDl() {

    }
}
