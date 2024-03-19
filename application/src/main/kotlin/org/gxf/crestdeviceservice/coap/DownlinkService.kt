// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.coap

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.gxf.crestdeviceservice.psk.PskService
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.springframework.stereotype.Service

@Service
class DownlinkService(private val pskService: PskService) {

    companion object{
        private const val URC_FIELD = "URC"

        private const val URC_PSK_SUCCESS = "PSK:SET"
        private const val URC_PSK_ERROR = "PSK:EQER"

        private const val RESPONSE_SUCCESS = "0"
    }

    private val logger = KotlinLogging.logger {}

    @Transactional
    fun getDownlinkForIdentity(identity: String, body: JsonNode): String {

        // Retrieve URCs from the message body
        val urcs = body[URC_FIELD].map { it.toString() }

        if (pskService.needsKeyChange(identity)) {
            logger.info { "Creating new key for device $identity" }

            val newKey = pskService.generateAndSetNewKeyForIdentity(identity)

            // After setting a new psk the device will send a new message if the psk set was successful
            return PskCommandCreator.createPskSetCommand(newKey)
        }

        if (urc.contains(URC_PSK_SUCCESS)) {
            pskService.changeActiveKey(identity)
        } else if (urc.contains(URC_PSK_ERROR)) {
            pskService.setLastKeyStatus(identity, PreSharedKeyStatus.INVALID)
            // todo alert naar maki?
        }

        return RESPONSE_SUCCESS
    }
}
