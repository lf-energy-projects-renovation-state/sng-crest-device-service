// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.generator

import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.device.service.DeviceService
import org.gxf.crestdeviceservice.psk.service.PskService
import org.springframework.stereotype.Component

@Component
class PskSetCommandGenerator(pskService: PskService, deviceService: DeviceService) :
    PskCommandGenerator(pskService, deviceService) {
    override fun getCommand(key: String, hash: String) = "PSK:$key:$hash:SET"

    override fun getSupportedCommand() = Command.CommandType.PSK_SET
}
