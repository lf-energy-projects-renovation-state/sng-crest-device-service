// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import com.alliander.sng.Command as ExternalCommand
import org.gxf.crestdeviceservice.TestConstants.CORRELATION_ID
import org.gxf.crestdeviceservice.TestConstants.DEVICE_ID
import org.gxf.crestdeviceservice.TestConstants.timestamp
import org.gxf.crestdeviceservice.command.entity.Command

object ExternalCommandFactory {
    fun externalRebootCommand() =
        ExternalCommand.newBuilder()
            .setDeviceId(DEVICE_ID)
            .setCorrelationId(CORRELATION_ID)
            .setTimestamp(timestamp)
            .setCommand(Command.CommandType.REBOOT.name)
            .setValue(null)
            .build()!!

    fun externalRebootCommandInvalid() =
        ExternalCommand.newBuilder()
            .setDeviceId(DEVICE_ID)
            .setCorrelationId(CORRELATION_ID)
            .setTimestamp(timestamp)
            .setCommand("unknown")
            .setValue(null)
            .build()!!
}
