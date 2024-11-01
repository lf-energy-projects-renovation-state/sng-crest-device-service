// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.device.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import java.util.Optional
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gxf.crestdeviceservice.device.entity.Device
import org.gxf.crestdeviceservice.device.exception.DuplicateDeviceException
import org.gxf.crestdeviceservice.device.exception.NoSuchDeviceException
import org.gxf.crestdeviceservice.device.repository.DeviceRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class DeviceServiceTest {
    companion object {
        private const val DEVICE_ID = "deviceId"
        private const val SECRET = "secret"
    }

    @MockK private lateinit var repository: DeviceRepository

    @InjectMockKs private lateinit var service: DeviceService

    @Test
    fun `should create device`() {
        every { repository.existsById(DEVICE_ID) } returns false
        every { repository.save(any()) } answers { firstArg() }

        val device = service.createDevice(DEVICE_ID, SECRET)
        assertThat(device.id).isEqualTo(DEVICE_ID)
        assertThat(device.secret).isEqualTo(SECRET)
    }

    @Test
    fun `should not create duplicate device`() {
        every { repository.existsById(DEVICE_ID) } returns true

        assertThatThrownBy { service.createDevice(DEVICE_ID, SECRET) }
            .isInstanceOf(DuplicateDeviceException::class.java)
    }

    @Test
    fun `should retrieve device`() {
        val device = Device(DEVICE_ID, SECRET)

        every { repository.findById(DEVICE_ID) } returns Optional.of(device)

        assertThat(service.getDevice(DEVICE_ID)).isSameAs(device)
    }

    @Test
    fun `should not retrieve nonexistent device`() {
        every { repository.findById(DEVICE_ID) } returns Optional.empty()

        assertThatThrownBy { service.getDevice(DEVICE_ID) }.isInstanceOf(NoSuchDeviceException::class.java)
    }
}
