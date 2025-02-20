// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.MessageFactory
import org.gxf.crestdeviceservice.TestConstants.ALARMS_INFO
import org.gxf.crestdeviceservice.TestConstants.ALARMS_INFO_DOWNLINK
import org.junit.jupiter.api.Test

class AlarmsInfoServiceTest {
    private val alarmsInfoService = AlarmsInfoService()

    @Test
    fun getAlarmsInfo() {
        val message =
            MessageFactory.messageWithUrc(
                listOf("""{"AL0":[0,1,0,1,0], "AL1":[0,0,0,0,0], "AL6":[0,500,1000,1500,10]}"""),
                ALARMS_INFO_DOWNLINK,
            )
        val expected = ALARMS_INFO

        val result = alarmsInfoService.getAlarmsInfo(message)

        assertThat(result).isEqualTo(expected)
    }
}
