// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.mapper

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.TestHelper
import org.gxf.crestdeviceservice.command.entity.Command
import org.junit.jupiter.api.Test

class CommandMapperTest {
    @Test
    fun externalCommandToCommandEntity() {
        val externalCommand = TestHelper.receivedRebootCommand()
        val status = Command.CommandStatus.PENDING
        val expected = TestHelper.pendingRebootCommand()

        val result = CommandMapper.externalCommandToCommandEntity(externalCommand, status)

        assertThat(result).usingRecursiveComparison().ignoringFields("id", "timestampIssued").isEqualTo(expected)
    }

    @Test
    fun translateCommand() {
        val command = "reboot"
        val result = CommandMapper.translateCommand(command)

        assertThat(result).isEqualTo("REBOOT")
    }
}
