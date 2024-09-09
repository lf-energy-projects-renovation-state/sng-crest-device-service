// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.mapper

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.ExternalCommandFactory
import org.gxf.crestdeviceservice.TestHelper
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.exception.CommandValidationException
import org.junit.jupiter.api.Test

class CommandMapperTest {
    @Test
    fun externalCommandToCommandEntity() {
        val externalCommand = ExternalCommandFactory.externalRebootCommand()
        val status = Command.CommandStatus.PENDING
        val expected = CommandFactory.pendingRebootCommand()

        val result = CommandMapper.externalCommandToCommandEntity(externalCommand, status)

        assertThat(result).usingRecursiveComparison().ignoringFields("id", "timestampIssued").isEqualTo(expected)
    }

    @Test
    fun externalCommandToCommandEntityException() {
        val externalCommand = ExternalCommandFactory.externalRebootCommandInvalid()
        val status = Command.CommandStatus.PENDING
        val expectedException = CommandValidationException("Command unknown")

        assertThatThrownBy {
            CommandMapper.externalCommandToCommandEntity(externalCommand, status)
        }.usingRecursiveComparison().isEqualTo(expectedException)
    }

    @Test
    fun commandNameToType() {
        val command = "reboot"
        val result = CommandMapper.commandNameToType(command)

        assertThat(result).isEqualTo(Command.CommandType.REBOOT)
    }

    @Test
    fun commandNameToTypeException() {
        val command = "invalid"

        assertThatThrownBy {
            CommandMapper.commandNameToType(command)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }
}
