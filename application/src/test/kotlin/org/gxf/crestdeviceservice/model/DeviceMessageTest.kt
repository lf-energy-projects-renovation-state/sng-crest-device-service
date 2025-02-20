// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DeviceMessageTest {
    private val mapper = jacksonObjectMapper()

    private val jsonMessage =
        """
        {
          "ID": 867787050253370,
          "URC": [
            {
              "DL": "!INFO:ALARMS"
            },
            {
              "AL0": [ 1, 2, 3, 4, 5 ],
              "AL1": [ 10, 20, 30, 40, 50 ],
              "AL2": [ 100, 200, 300, 400, 500 ]
            }
          ],
          "FMC": 3
        }
    """
            .trimIndent()
    private val deviceMessage = DeviceMessage(mapper.readTree(jsonMessage))

    @Test
    fun `should return DL command from URC`() {
        assertThat(deviceMessage.getDownlinkCommand()).isEqualTo("!INFO:ALARMS")
    }

    @Test
    fun `should return object containing AL2`() {
        assertThat(deviceMessage.getUrcContainingField("AL2")).isNotNull
        assertThat(deviceMessage.getUrcContainingField("AL2").findValue("AL1").map { it.intValue() })
            .containsExactly(10, 20, 30, 40, 50)
    }

    @Test
    fun `should return Fota Message Counter`() {
        assertThat(deviceMessage.getFotaMessageCounter()).isEqualTo(3)
    }
}
