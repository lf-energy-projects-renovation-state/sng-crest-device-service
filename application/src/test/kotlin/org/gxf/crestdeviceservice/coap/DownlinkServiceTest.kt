package org.gxf.crestdeviceservice.coap

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.psk.PskService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any

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

        assertThat("0").isEqualTo(result)
    }

    @Test
    fun shouldReturnPskDownlinkWhenThereIsANewPsk() {
        `when`(pskService.hasDefaultKey(any())).thenReturn(true)
        `when`(pskService.generateAndSetNewKeyForIdentity(any())).thenReturn("key")

        val result = downLinkService.getDownlinkForIdentity("identity")

        // Psk command is formatted as: PSK:[Key];PSK:[Key]SET
        assertThat("PSK:key;PSK:keySET").isEqualTo(result)
    }
}
