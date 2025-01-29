// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.MessageFactory
import org.gxf.crestdeviceservice.TestConstants.ALARMS_INFO_DOWNLINK
import org.gxf.crestdeviceservice.model.AlarmsInfo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AlarmsInfoServiceTest {
    @InjectMockKs lateinit var alarmsInfoService: AlarmsInfoService

    @Test
    fun getAlarmsInfo() {
        val message = MessageFactory.messageWithUrc(listOf(), ALARMS_INFO_DOWNLINK)
        val expected =
            AlarmsInfo(AL0 = listOf(0, 1, 0, 1, 0), AL1 = listOf(0, 0, 0, 0, 0), AL6 = listOf(100, 200, 300, 400, 10))

        val result = alarmsInfoService.getAlarmsInfo(message)

        assertThat(result).isEqualTo(expected)
    }
}
