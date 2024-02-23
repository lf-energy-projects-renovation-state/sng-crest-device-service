package org.gxf.crestdeviceservice.psk

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.psk.entity.PskRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any

@ExtendWith(MockitoExtension::class)
class PskServiceTest {

    @Mock
    private lateinit var pskRepository: PskRepository

    @Mock
    private lateinit var pskConfiguration: PskConfiguration

    @InjectMocks
    private lateinit var pskService: PskService

    @Test
    fun needsKeyChange() {
        // If change initial psk is true, and we only have one key the key should be changed
        `when`(pskRepository.countPsksByIdentity(any())).thenReturn(1L)
        `when`(pskConfiguration.changeInitialPsk).thenReturn(true)

        assertThat(pskService.needsKeyChange("123")).isTrue()

        // If change initial psk we shouldn't change the key
        `when`(pskConfiguration.changeInitialPsk).thenReturn(false)

        assertThat(pskService.needsKeyChange("123")).isFalse()

        // If we have 0 keys we shouldn't generate a new key
        `when`(pskRepository.countPsksByIdentity(any())).thenReturn(0L)
        `when`(pskConfiguration.changeInitialPsk).thenReturn(true)

        assertThat(pskService.needsKeyChange("123")).isFalse()

        // If we have more than one key we shouldn't generate a new key
        `when`(pskRepository.countPsksByIdentity(any())).thenReturn(2L)
        `when`(pskConfiguration.changeInitialPsk).thenReturn(true)

        assertThat(pskService.needsKeyChange("123")).isFalse()
    }
}
