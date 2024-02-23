// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.coap

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.gxf.crestdeviceservice.psk.PskService
import org.springframework.stereotype.Service

@Service
class DownlinkService(private val pskService: PskService) {

    private val logger = KotlinLogging.logger {}

    @Transactional
    fun getDownlinkForIdentity(identity: String): String {

        if (pskService.needsKeyChange(identity)) {
            logger.info { "Device $identity has default key creating new key" }

            val newKey = pskService.generateAndSetNewKeyForIdentity(identity)

            return PskCommandCreator.createPskSetCommand(newKey)
        }

        return "0"
    }
}
