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
        private const val RESPONSE_SUCCESS = "0"
    }

    private val logger = KotlinLogging.logger {}

    @Transactional
    fun getDownlinkForIdentity(identity: String, body: JsonNode): String {

        // Retrieve URCs from the message body
        val urc = body[URC_FIELD]
            .filter { it.isTextual }
            .map { it.asText() }

        if (pskService.needsKeyChange(identity)) {
            logger.info { "Creating new key for device $identity" }

            val newKey = pskService.generateAndSetNewKeyForIdentity(identity)

            // After setting a new psk the device will send a new message if the psk set was successful
            return PskCommandCreator.createPskSetCommand(newKey)
        }

        val pendingPsk = pskService.getCurrentPskWithStatus(identity, PreSharedKeyStatus.PENDING)
        check(pendingPsk != null) { "There is no known pending PSK for id $identity" }
        val successMessage = PskCommandCreator.createPskSetCommand(pendingPsk)
        val errorMessage = PskCommandCreator.createPskErrorCommand(pendingPsk)
        if (urc.contains(successMessage)) {
            pskService.changeActiveKey(identity)
        } else if (urc.contains(errorMessage)) {
            pskService.setLastKeyStatus(identity, PreSharedKeyStatus.INVALID)
            // todo alert naar maki?
        } else {
            error("Cannot interpret this URC: $urc")
        }

        return RESPONSE_SUCCESS
    }
}
