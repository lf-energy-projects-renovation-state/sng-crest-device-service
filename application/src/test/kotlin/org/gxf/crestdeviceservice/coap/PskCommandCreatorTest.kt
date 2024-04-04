// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.coap

import org.assertj.core.api.Assertions
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Instant

class PskCommandCreatorTest {

    @ParameterizedTest
    @CsvSource(
            "1234567890123456,1234,390d4757bf1b75e305984c99cdedfb1e7c201a2d143a53cfbc35075fa5f9a56f,secret",
            "1234567890123456,2345,c1f50bc9d85835bb6077aa3577b030cf8a17a3db7e8b42b83011cae8538f26cd,different-secret",
            "6543210987654321,3456,143ecd0dffadbbc248748c8725313c77d0d1eb297c90719804bc0cc361580283,secret",
            "6543210987654321,4567,c3d193f4726807f6fb8dca6171ddadfb9f8f15f5a599d710e3b507534f1602c2,different-secret"
    )
    fun shouldCreateACorrectPskCommandoWithHash(key: String, oldKey: String, expectedHash: String, usedSecret: String) {
        val preSharedKey =
            PreSharedKey("identity", 0, Instant.now(), key, usedSecret, PreSharedKeyStatus.PENDING)

        val result = PskCommandCreator.createPskSetCommand(preSharedKey, oldKey)

        // Psk command is formatted as: PSK:[Key]:[Hash];PSK:[Key]:[Hash]:SET
        Assertions.assertThat(result).isEqualTo("!PSK:${key}:${expectedHash};PSK:${key}:${expectedHash}:SET")
    }
}
