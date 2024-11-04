// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.psk.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.psk.PSKTestHelper
import org.gxf.crestdeviceservice.psk.configuration.PskConfiguration
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus.ACTIVE
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus.INACTIVE
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus.INVALID
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus.PENDING
import org.gxf.crestdeviceservice.psk.repository.PskRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PskServiceTest {
    @MockK private lateinit var pskRepository: PskRepository

    @MockK private lateinit var pskConfiguration: PskConfiguration

    @InjectMockKs private lateinit var pskService: PskService

    @Test
    fun getCurrentActiveKey() {
        val deviceId = "identity"
        val psk = PSKTestHelper.preSharedKeyActive()

        every { pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(deviceId, ACTIVE) } returns psk

        val currentActiveKey = pskService.getCurrentActiveKey(deviceId)

        assertThat(currentActiveKey).isEqualTo(psk.preSharedKey)
    }

    @Test
    fun pendingKeyPresentTrue() {
        val deviceId = "identity"
        val psk = PSKTestHelper.preSharedKeyPending()

        every { pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(deviceId, PENDING) } returns psk

        val pendingKeyPresent = pskService.isPendingPskPresent(deviceId)

        assertThat(pendingKeyPresent).isTrue
    }

    @Test
    fun pendingKeyPresentFalse() {
        val deviceId = "identity"

        every { pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(deviceId, PENDING) } returns null

        val pendingKeyPresent = pskService.isPendingPskPresent(deviceId)

        assertThat(pendingKeyPresent).isFalse
    }

    @Test
    fun setPendingKeyAsInvalid() {
        val psk = PSKTestHelper.preSharedKeyPending()
        val deviceId = psk.identity

        every { pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(deviceId, PENDING) } returns psk
        every { pskRepository.save(any()) } answers { firstArg() }

        pskService.setPendingKeyAsInvalid(deviceId)

        val pskSlot = slot<PreSharedKey>()

        verify { pskRepository.save(capture(pskSlot)) }

        assertThat(pskSlot.captured.status).isEqualTo(INVALID)
    }

    @Test
    fun saveReadyKeyForIdentityAsPending() {
        val psk = PSKTestHelper.preSharedKeyReady()
        val deviceId = psk.identity
        val status = psk.status

        every { pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(deviceId, status) } returns psk
        every { pskRepository.save(any()) } answers { firstArg() }

        val result = pskService.setPskToPendingForDevice(deviceId)
        assertThat(result.status).isEqualTo(PENDING)

        val pskSlot = slot<PreSharedKey>()

        verify { pskRepository.save(capture(pskSlot)) }

        assertThat(pskSlot.captured.status).isEqualTo(PENDING)
    }

    @Test
    fun changeActiveKey() {
        val deviceId = "identity"
        val currentPsk = PSKTestHelper.preSharedKeyActive()
        val newPsk = PSKTestHelper.preSharedKeyPending()

        every { pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(deviceId, ACTIVE) } returns currentPsk
        every { pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(deviceId, PENDING) } returns newPsk
        every { pskRepository.saveAll(any<Iterable<PreSharedKey>>()) } answers { firstArg() }

        pskService.changeActiveKey(deviceId)

        val pskListSlot = slot<List<PreSharedKey>>()

        verify { pskRepository.saveAll(capture(pskListSlot)) }

        val savedPsks = pskListSlot.captured
        assertThat(savedPsks).hasSize(2)
        assertThat(savedPsks.first().status).isEqualTo(INACTIVE)
        assertThat(savedPsks.last().status).isEqualTo(ACTIVE)
    }
}
