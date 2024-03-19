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
        private const val INIT_MESSAGE = "INIT"
        private const val RESPONSE_SUCCESS = "0"
    }

    private val logger = KotlinLogging.logger {}

    @Transactional
    fun getDownlinkForIdentity(identity: String, messageBody: JsonNode): String {
        if (pskService.needsKeyChange(identity)) {
            logger.info { "Creating new key for device $identity" }

            val newKey = pskService.generateAndSetNewKeyForIdentity(identity)

            // After setting a new psk, the device will send a new message if the psk set was successful
            return PskCommandCreator.createPskSetCommand(newKey)
        }

        interpretURCInMessage(identity, messageBody)

        return RESPONSE_SUCCESS
    }

    private fun interpretURCInMessage(identity: String, messageBody: JsonNode) {
        // Retrieve URCs from the message body
        val urc = messageBody[URC_FIELD]
            .filter { it.isTextual }
            .map { it.asText() }

        val pendingPsk = pskService.getCurrentPskWithStatus(identity, PreSharedKeyStatus.PENDING)
        check(pendingPsk != null) { "There is no known pending PSK for id $identity" }

        val successMessage = PskCommandCreator.createPskSetCommand(pendingPsk)
        val errorMessage = PskCommandCreator.createPskErrorCommand(pendingPsk)

        when {
            urc.contains(successMessage) -> {
                pskService.changeActiveKey(identity)
            }

            urc.contains(errorMessage) -> {
                pskService.setLastKeyStatus(identity, PreSharedKeyStatus.INVALID)
                // todo alert naar maki?
            }

            urc.contains(INIT_MESSAGE) -> {
                error("Regular message while PSK is pending")
            }

            else -> {
                error("Cannot interpret this URC: $urc")
            }
        }
    }
}
