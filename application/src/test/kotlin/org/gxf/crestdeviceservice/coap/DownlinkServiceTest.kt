package org.gxf.crestdeviceservice.coap

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.psk.PskService
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class DownlinkServiceTest {

    @Mock
    private lateinit var pskService: PskService

    @InjectMocks
    private lateinit var downLinkService: DownlinkService

    @Test
    fun shouldReturnNoActionDownlinkWhenThereIsNoNewPsk() {
        `when`(pskService.hasDefaultKey(any())).thenReturn(false)

        val result = downLinkService.getDownlinkForIdentity("identity")

        assertThat(result).isEqualTo("0")
    }

    @Test
    fun shouldReturnPskDownlinkWhenThereIsANewPsk() {
        val expectedKey = "key"
        val expectedHash = "ad165b11320bc91501ab08613cc3a48a62a6caca4d5c8b14ca82cc313b3b96cd"

        `when`(pskService.hasDefaultKey(any())).thenReturn(true)
        `when`(pskService.generateAndSetNewKeyForIdentity(any())).thenReturn(PreSharedKey("identity", Instant.now(), expectedKey, "secret"))

        val result = downLinkService.getDownlinkForIdentity("identity")

        // Psk command is formatted as: PSK:[Key][Hash];PSK:[Key][Hash]SET
        assertThat(result).isEqualTo("!PSK:$expectedKey$expectedHash;PSK:$expectedKey${expectedHash}SET")
    }
}
