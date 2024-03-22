package org.gxf.crestdeviceservice.psk

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.TestHelper
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.gxf.crestdeviceservice.psk.entity.PskRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever


@ExtendWith(MockitoExtension::class)
class PskServiceTest {

    @Mock
    private lateinit var pskRepository: PskRepository

    @Mock
    private lateinit var pskConfiguration: PskConfiguration

    @InjectMocks
    private lateinit var pskService: PskService

    @Test
    fun getCurrentActiveKey() {
        val psk = TestHelper.preSharedKeyActive()
        whenever(pskRepository.findLatestPskForIdentityWithStatus(any(), any())).thenReturn(psk)

        assertThat(psk.preSharedKey).isEqualTo(pskService.getCurrentActiveKey("identity"))
    }

    @Test
    fun pendingKeyPresentTrue() {
        val psk = TestHelper.preSharedKeyPending()
        whenever(pskRepository.findLatestPskForIdentityWithStatus(any(), any())).thenReturn(psk)

        assertThat(pskService.pendingKeyPresent("identity")).isEqualTo(true)
    }

    @Test
    fun pendingKeyPresentFalse() {
        whenever(pskRepository.findLatestPskForIdentityWithStatus(any(), any())).thenReturn(null)

        assertThat(pskService.pendingKeyPresent("identity")).isEqualTo(false)
    }

    @Test
    fun setPendingKeyAsInvalid() {
        val psk = TestHelper.preSharedKeyPending()
        whenever(pskRepository.findLatestPskForIdentityWithStatus(any(), any())).thenReturn(psk)

        psk.status = PreSharedKeyStatus.INVALID

        pskService.setPendingKeyAsInvalid("identity")

        verify(pskRepository).save(psk)
    }

    @Test
    fun saveReadyKeyForIdentityAsPending() {
        val psk = TestHelper.preSharedKeyReady()
        psk.status = PreSharedKeyStatus.PENDING

        whenever(
            pskRepository.findLatestPskForIdentityWithStatus(
                any<String>(),
                any<PreSharedKeyStatus>()
            )
        ).thenReturn(psk)
        whenever(pskRepository.save(psk)).thenReturn(psk)

        assertThat(psk).isEqualTo(pskService.saveReadyKeyForIdentityAsPending("id123"))
    }

    @Test
    fun needsKeyChangeSetInitialPskTrueAnd1ReadyIdentity() {
        // If change initial psk is true, and we have a ready key, the key should be changed
        whenever(pskRepository.findLatestPskForIdentityWithStatus(any(), any())).thenReturn(
            TestHelper.preSharedKeyReady()
        )
        whenever(pskConfiguration.changeInitialPsk).thenReturn(true)

        assertThat(pskService.needsKeyChange("123")).isTrue()
    }

    @Test
    fun needsKeyChangeSetInitialPskFalseAnd1ReadyIdentity() {
        // If change initial psk is false we shouldn't change the key
        whenever(pskConfiguration.changeInitialPsk).thenReturn(false)

        assertThat(pskService.needsKeyChange("123")).isFalse()
    }

    @Test
    fun needsKeyChangeSetInitialPskTrueAnd0ReadyIdentity() {
        // If we have 0 keys we shouldn't generate a new key
        whenever(pskRepository.findLatestPskForIdentityWithStatus(any(), any())).thenReturn(null)
        whenever(pskConfiguration.changeInitialPsk).thenReturn(true)

        assertThat(pskService.needsKeyChange("123")).isFalse()
    }

    @Test
    fun changeActiveKey() {
        val identity = "identity"
        val currentPsk = TestHelper.preSharedKeyActive()
        val newPsk = TestHelper.preSharedKeyPending()
        whenever(
            pskRepository.findLatestPskForIdentityWithStatus(
                identity,
                PreSharedKeyStatus.ACTIVE
            )
        ).thenReturn(currentPsk)
        whenever(
            pskRepository.findLatestPskForIdentityWithStatus(
                identity,
                PreSharedKeyStatus.PENDING
            )
        ).thenReturn(newPsk)

        currentPsk.status = PreSharedKeyStatus.INACTIVE
        newPsk.status = PreSharedKeyStatus.ACTIVE

        pskService.changeActiveKey(identity)

        val argument = ArgumentCaptor.forClass(
            PreSharedKey::class.java
        )

        verify(pskRepository, times(2)).save(argument.capture())

        val values = argument.allValues

        assertThat(values.contains(currentPsk)).isTrue()
        assertThat(values.contains(newPsk)).isTrue()
    }
}
