// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.coap

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.gxf.crestdeviceservice.psk.PskService
import org.springframework.stereotype.Service

@Service
class DownlinkService(private val pskService: PskService) {

    companion object{
        private const val URC_PSK_SUCCESS = "PSK:SET"
        private const val URC_PSK_ERROR = "ER"
        private const val RESPONSE_SUCCESS = "0"
    }

    private val logger = KotlinLogging.logger {}

    @Transactional
    fun getDownlinkForIdentity(identity: String, urcList: JsonNode): String {
        if (pskService.needsKeyChange(identity)) {
            val newKey = pskService.getNewKeyForIdentityAsPending(identity)
            // After setting a new psk, the device will send a new message if the psk set was successful
            return PskCommandCreator.createPskSetCommand(newKey)
        } else if (pskService.pendingKeyPresent(identity)) {
            interpretURCInMessage(identity, urcList)
        }

        return RESPONSE_SUCCESS
    }

    private fun interpretURCInMessage(identity: String, urcList: JsonNode) {
        // Retrieve URCs from the message body
        val urc = urcList
            .filter { it.isTextual }
            .map { it.asText() }

        when {
            urc.contains(URC_PSK_SUCCESS) -> {
                logger.info { "PSK set successful, changing active key" }
                pskService.changeActiveKey(identity)
            }

            urc.contains(URC_PSK_ERROR) -> {
                logger.warn { "Error received for set PSK command, setting pending key to invalid" }
                pskService.setLastKeyInvalid(identity)
            }
        }
    }
}
