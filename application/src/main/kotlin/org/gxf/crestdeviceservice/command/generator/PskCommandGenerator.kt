// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.generator

import org.apache.commons.codec.digest.DigestUtils
import org.gxf.crestdeviceservice.command.entity.Command
import org.gxf.crestdeviceservice.device.service.DeviceService
import org.gxf.crestdeviceservice.psk.exception.NoExistingPskException
import org.gxf.crestdeviceservice.psk.service.PskService
import org.springframework.stereotype.Component

@Component
class PskCommandGenerator(private val pskService: PskService, private val deviceService: DeviceService) :
    CommandGenerator {
    override fun generateCommandString(command: Command): String {
        val readyKey =
            pskService.getCurrentReadyPsk(command.deviceId)
                ?: throw NoExistingPskException("There is no new key ready to be set")
        val secret = deviceService.getDevice(command.deviceId).secret

        val newKey = readyKey.preSharedKey
        val hash = DigestUtils.sha256Hex("$secret$newKey")
        return getCommand(newKey, hash)
    }

    fun getCommand(key: String, hash: String) = "PSK:$key:$hash"

    override fun getSupportedCommand() = Command.CommandType.PSK
}
