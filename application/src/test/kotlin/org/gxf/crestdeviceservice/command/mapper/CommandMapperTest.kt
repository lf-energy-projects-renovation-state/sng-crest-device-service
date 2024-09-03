package org.gxf.crestdeviceservice.command.mapper

import org.assertj.core.api.Assertions.assertThat

import org.junit.jupiter.api.Test

class CommandMapperTest {
    @Test
    fun translateCommand() {
        val command = "reboot"
        val result = CommandMapper.translateCommand(command)

        assertThat(result).isEqualTo("REBOOT")
    }
}