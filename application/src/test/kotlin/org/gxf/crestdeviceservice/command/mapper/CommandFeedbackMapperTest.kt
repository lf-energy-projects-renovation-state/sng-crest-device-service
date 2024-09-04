package org.gxf.crestdeviceservice.command.mapper

import org.assertj.core.api.Assertions.assertThat
import com.alliander.sng.CommandStatus as ExternalCommandStatus
import org.gxf.crestdeviceservice.TestHelper

import org.junit.jupiter.api.Test

class CommandFeedbackMapperTest {
    private val status = ExternalCommandStatus.Received
    private val message = TestHelper.receivedMessage()
    private val expected = TestHelper.rebootCommandReceivedFeedback()

    @Test
    fun externalCommandToCommandFeedback() {
        val externalCommand = TestHelper.receivedRebootCommand()

        val result = CommandFeedbackMapper.externalCommandToCommandFeedback(externalCommand, status, message)

        assertThat(result).usingRecursiveComparison().ignoringFields("timestampStatus").isEqualTo(expected)
    }

    @Test
    fun commandEntityToCommandFeedback() {
        val commandEntity = TestHelper.pendingRebootCommand()

        val result = CommandFeedbackMapper.commandEntityToCommandFeedback(commandEntity, status, message)

        assertThat(result).usingRecursiveComparison().ignoringFields("timestampStatus").isEqualTo(expected)
    }
}