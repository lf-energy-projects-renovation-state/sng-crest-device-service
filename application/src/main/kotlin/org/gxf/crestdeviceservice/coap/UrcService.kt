// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.coap

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.psk.PskService
import org.gxf.crestdeviceservice.psk.exception.NoExistingPskException
import org.springframework.stereotype.Service

@Service
class UrcService(
    private val pskService: PskService
) {
    companion object {
        private const val URC_PSK_SUCCESS = "PSK:SET"
        private const val URC_PSK_ERROR = "ER"
        private const val URC_FIELD = "URC"
    }

    private val logger = KotlinLogging.logger {}

    @Throws(NoExistingPskException::class)
    fun interpretURCInMessage(identity: String, body: JsonNode) {
        val urcs = getUrcsFromMessage(body)
        if (urcs.isEmpty()) {
            logger.debug { "Received message without urcs" }
            return
        }
        logger.debug { "Received message with urcs ${urcs.joinToString(", ")}" }

        when {
            urcsContainsSuccess(urcs) -> {
                handleSuccessUrc(identity)
            }

            urcsContainsError(urcs) -> {
                handleErrorUrc(identity)
            }
        }
    }

    private fun getUrcsFromMessage(body: JsonNode) = body[URC_FIELD]
        .filter { it.isTextual }
        .map { it.asText() }

    private fun urcsContainsSuccess(urcs: List<String>) =
        urcs.any { urc -> urc.contains(URC_PSK_SUCCESS) }

    private fun handleSuccessUrc(identity: String) {
        if (!pskService.isPendingKeyPresent(identity)) {
            throw NoExistingPskException("Success URC received, but no pending key present to set as active")
        }
        logger.info { "PSK set successfully, changing active key" }
        pskService.changeActiveKey(identity)
    }

    private fun urcsContainsError(urcs: List<String>) =
        urcs.any { urc -> urc.contains(URC_PSK_ERROR) }

    private fun handleErrorUrc(identity: String) {
        if (!pskService.isPendingKeyPresent(identity)) {
            throw NoExistingPskException("Failure URC received, but no pending key present to set as invalid")
        }
        logger.warn { "Error received for set PSK command, setting pending key to invalid" }
        pskService.setPendingKeyAsInvalid(identity)
    }
}
