// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.coap

import org.apache.commons.codec.digest.DigestUtils
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey

object PskCommandCreator {

    fun createPskSetCommand(preSharedKey: PreSharedKey): String {
        val key = preSharedKey.preSharedKey
        val secret = preSharedKey.secret
        val hash = DigestUtils.sha256Hex("$secret$key")
        return "PSK:${key}${hash};PSK:${key}${hash}SET!"
    }
}
