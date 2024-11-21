// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import org.gxf.crestdeviceservice.CommandFactory
import org.gxf.crestdeviceservice.MessageFactory
import org.gxf.crestdeviceservice.TestConstants.DEVICE_ID
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.command.resulthandler.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CommandResultServiceTest {
    @MockK private lateinit var rebootCommandResultHandler: RebootCommandResultHandler
    @MockK private lateinit var rspCommandResultHandler: RspCommandResultHandler

    @MockK private lateinit var commandService: CommandService
    @MockK private lateinit var commandResultHandlersByType: Map<Command.CommandType, CommandResultHandler>

    @InjectMockKs private lateinit var commandResultService: CommandResultService

    @BeforeEach
    fun setUp() {
        every { commandResultHandlersByType[Command.CommandType.REBOOT] } answers { rebootCommandResultHandler }
        every { commandResultHandlersByType[Command.CommandType.RSP] } answers { rspCommandResultHandler }
    }

    @Test
    fun shouldHandleMessageWhenCommandHasSucceeded() {
        val message = MessageFactory.messageTemplate()
        val command = CommandFactory.rebootCommandInProgress()

        every { commandService.getAllCommandsInProgressForDevice(any()) } returns listOf(command)
        every { rebootCommandResultHandler.hasSucceeded(any(), any()) } returns true
        justRun { rebootCommandResultHandler.handleSuccess(any()) }

        commandResultService.handleMessage(DEVICE_ID, message)

        verify(exactly = 1) { rebootCommandResultHandler.hasSucceeded(DEVICE_ID, message) }
        verify(exactly = 0) { rebootCommandResultHandler.hasFailed(DEVICE_ID, message) }
        verify(exactly = 1) { rebootCommandResultHandler.handleSuccess(command) }
        verify(exactly = 0) { rebootCommandResultHandler.handleFailure(command, message) }
    }

    @Test
    fun shouldHandleMessageWhenCommandHasFailed() {
        val message = MessageFactory.messageTemplate()
        val command = CommandFactory.rspCommandInProgress()

        every { commandService.getAllCommandsInProgressForDevice(any()) } returns listOf(command)
        every { rspCommandResultHandler.hasSucceeded(any(), any()) } returns false
        every { rspCommandResultHandler.hasFailed(any(), any()) } returns true
        justRun { rspCommandResultHandler.handleFailure(any(), any()) }

        commandResultService.handleMessage(DEVICE_ID, message)

        verify(exactly = 1) { rspCommandResultHandler.hasSucceeded(DEVICE_ID, message) }
        verify(exactly = 0) { rspCommandResultHandler.handleSuccess(command) }
        verify(exactly = 1) { rspCommandResultHandler.hasFailed(DEVICE_ID, message) }
        verify(exactly = 1) { rspCommandResultHandler.handleFailure(command, message) }
    }

    @Test
    fun shouldHandleMessageWhenCommandIsStillInProgress() {
        val message = MessageFactory.messageTemplate()
        val command = CommandFactory.rspCommandInProgress()

        every { commandService.getAllCommandsInProgressForDevice(any()) } returns listOf(command)
        every { rspCommandResultHandler.hasSucceeded(any(), any()) } returns false
        every { rspCommandResultHandler.hasFailed(any(), any()) } returns false
        justRun { rspCommandResultHandler.handleStillInProgress(any()) }

        commandResultService.handleMessage(DEVICE_ID, message)

        verify(exactly = 1) { rspCommandResultHandler.hasSucceeded(DEVICE_ID, message) }
        verify(exactly = 0) { rspCommandResultHandler.handleSuccess(command) }
        verify(exactly = 1) { rspCommandResultHandler.hasFailed(DEVICE_ID, message) }
        verify(exactly = 0) { rspCommandResultHandler.handleFailure(command, message) }
        verify(exactly = 1) { rspCommandResultHandler.handleStillInProgress(command) }
    }

    @Test
    fun shouldHandleMessageWhenMultipleCommandsHaveSucceeded() {
        val message = MessageFactory.messageTemplate()
        val rebootCommand = CommandFactory.rebootCommandInProgress()
        val rspCommand = CommandFactory.rspCommandInProgress()

        every { commandService.getAllCommandsInProgressForDevice(any()) } returns listOf(rebootCommand, rspCommand)
        every { rebootCommandResultHandler.hasSucceeded(any(), any()) } returns true
        justRun { rebootCommandResultHandler.handleSuccess(any()) }
        every { rspCommandResultHandler.hasSucceeded(any(), any()) } returns true
        justRun { rspCommandResultHandler.handleSuccess(any()) }

        commandResultService.handleMessage(DEVICE_ID, message)

        verify(exactly = 1) { rebootCommandResultHandler.hasSucceeded(DEVICE_ID, message) }
        verify(exactly = 1) { rebootCommandResultHandler.handleSuccess(rebootCommand) }
        verify(exactly = 0) { rebootCommandResultHandler.hasFailed(DEVICE_ID, message) }
        verify(exactly = 0) { rebootCommandResultHandler.handleFailure(rebootCommand, message) }

        verify(exactly = 1) { rspCommandResultHandler.hasSucceeded(DEVICE_ID, message) }
        verify(exactly = 1) { rspCommandResultHandler.handleSuccess(rspCommand) }
        verify(exactly = 0) { rspCommandResultHandler.hasFailed(DEVICE_ID, message) }
        verify(exactly = 0) { rspCommandResultHandler.handleFailure(rspCommand, message) }
    }

    @Test
    fun handleMessageWhenOneCommandHasSucceededAndOneCommandHasFailed() {
        val message = MessageFactory.messageTemplate()
        val rebootCommand = CommandFactory.rebootCommandInProgress()
        val rspCommand = CommandFactory.rspCommandInProgress()

        every { commandService.getAllCommandsInProgressForDevice(any()) } returns listOf(rebootCommand, rspCommand)
        every { rebootCommandResultHandler.hasSucceeded(any(), any()) } returns true
        justRun { rebootCommandResultHandler.handleSuccess(any()) }
        every { rspCommandResultHandler.hasSucceeded(any(), any()) } returns false
        every { rspCommandResultHandler.hasFailed(any(), any()) } returns true
        justRun { rspCommandResultHandler.handleFailure(any(), any()) }

        commandResultService.handleMessage(DEVICE_ID, message)

        verify(exactly = 1) { rebootCommandResultHandler.hasSucceeded(DEVICE_ID, message) }
        verify(exactly = 1) { rebootCommandResultHandler.handleSuccess(rebootCommand) }
        verify(exactly = 0) { rebootCommandResultHandler.hasFailed(DEVICE_ID, message) }
        verify(exactly = 0) { rebootCommandResultHandler.handleFailure(rebootCommand, message) }

        verify(exactly = 1) { rspCommandResultHandler.hasSucceeded(DEVICE_ID, message) }
        verify(exactly = 0) { rspCommandResultHandler.handleSuccess(rspCommand) }
        verify(exactly = 1) { rspCommandResultHandler.hasFailed(DEVICE_ID, message) }
        verify(exactly = 1) { rspCommandResultHandler.handleFailure(rspCommand, message) }
    }
}
