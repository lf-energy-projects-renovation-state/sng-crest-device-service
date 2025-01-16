// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.mapper

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.ExternalCommandFactory
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.exception.CommandValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

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
    fun externalCommandToCommandEntityAlarmThresholds() {
        val externalCommand = ExternalCommandFactory.externalAnalogAlarmThresholdsPort3Command()
        val status = Command.CommandStatus.PENDING
        val expected = CommandFactory.pendingAnalogAlarmThresholdsPort3Command()

        val result = CommandMapper.externalCommandToCommandEntity(externalCommand, status)

        assertThat(result).usingRecursiveComparison().ignoringFields("id", "timestampIssued").isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("invalidCommandValues")
    fun externalCommandToCommandEntityAlarmThresholdsException(value: String) {
        val externalCommand = ExternalCommandFactory.externalAnalogAlarmThresholdsPort3Command()
        val status = Command.CommandStatus.PENDING
        val expected = CommandFactory.pendingAnalogAlarmThresholdsPort3Command()

        val result = CommandMapper.externalCommandToCommandEntity(externalCommand, status)

        assertThat(result).usingRecursiveComparison().ignoringFields("id", "timestampIssued").isEqualTo(expected)
    }

    companion object {
        @JvmStatic
        private fun invalidCommandValues() =
            listOf(
                "",
                "0,1250,2500,3750,100",
                "5:0,1250,2500,3750,100",
                "L:0,1250,2500,3750,100",
                "4:bla",
                "4:0,1.25,2.5,3.75,0.1",
                "4,0,1250,2500,3750,100",
                "3:een,twee,drie,vier",
            )
    }

    @Test
    fun externalCommandToCommandEntityException() {
        val externalCommand = ExternalCommandFactory.externalRebootCommandInvalid()
        val status = Command.CommandStatus.PENDING

        val actual = catchThrowable { CommandMapper.externalCommandToCommandEntity(externalCommand, status) }

        assertThat(actual)
            .isInstanceOf(CommandValidationException::class.java)
            .hasMessage("Command unknown: ${externalCommand.command}")
    }

    @Test
    fun commandNameToType() {
        val commandName = "reboot"
        val result = CommandMapper.commandNameToType(commandName)

        assertThat(result).isEqualTo(Command.CommandType.REBOOT)
    }

    @Test
    fun commandNameToTypeException() {
        val commandName = "invalid"

        val actual = catchThrowable { CommandMapper.commandNameToType(commandName) }

        assertThat(actual).isInstanceOf(IllegalArgumentException::class.java)
    }
}
