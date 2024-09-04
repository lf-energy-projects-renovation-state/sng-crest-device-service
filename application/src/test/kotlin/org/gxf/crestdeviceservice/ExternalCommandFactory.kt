package org.gxf.crestdeviceservice

import org.gxf.crestdeviceservice.TestHelper.CORRELATION_ID
import org.gxf.crestdeviceservice.TestHelper.DEVICE_ID
import org.gxf.crestdeviceservice.TestHelper.timestamp
import org.gxf.crestdeviceservice.command.entity.Command

object ExternalCommandFactory {
    fun externalRebootCommand() =
        com.alliander.sng.Command.newBuilder()
            .setDeviceId(DEVICE_ID)
            .setCorrelationId(CORRELATION_ID)
            .setTimestamp(timestamp)
            .setCommand(Command.CommandType.REBOOT.name)
            .setValue(null)
            .build()!!
}