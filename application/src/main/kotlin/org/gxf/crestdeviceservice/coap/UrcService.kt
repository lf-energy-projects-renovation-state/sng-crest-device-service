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
class UrcService(private val pskService: PskService) {
    companion object {
        private const val URC_FIELD = "URC"
        private const val URC_PSK_SUCCESS = "PSK:SET"
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
            urcsContainPskOrDownlinkError(urcs) -> {
                handlePskOrDownlinkErrors(identity, urcs)
            }
            urcsContainPskSuccess(urcs) -> {
                handlePskSuccess(identity)
            }
        }
    }

    private fun getUrcsFromMessage(body: JsonNode) =
        body[URC_FIELD].filter { it.isTextual }.map { it.asText() }

    private fun urcsContainPskOrDownlinkError(urcs: List<String>) =
        urcs.any { urc -> isPskOrDownlinkErrorURC(urc) }

    private fun isPskOrDownlinkErrorURC(urc: String) =
        PskOrDownlinkErrorUrc.entries.toTypedArray().contains(PskOrDownlinkErrorUrc.from(urc))

    private fun handlePskOrDownlinkErrors(identity: String, urcs: List<String>) {
        urcs
            .stream()
            .filter { urc -> isPskOrDownlinkErrorURC(urc) }
            .forEach { urc -> handlePskOrDownlinkError(identity, urc) }
    }

    private fun handlePskOrDownlinkError(identity: String, urc: String) {
        if (!pskService.isPendingKeyPresent(identity)) {
            throw NoExistingPskException(
                "Failure URC received, but no pending key present to set as invalid")
        }
        val errorMessage =
            when (PskOrDownlinkErrorUrc.from(urc)) {
                PskOrDownlinkErrorUrc.PSK_EQER -> "Set PSK does not equal earlier PSK"
                PskOrDownlinkErrorUrc.DL_UNK -> "Downlink unknown"
                PskOrDownlinkErrorUrc.DL_DLNA -> "Downlink not allowed"
                PskOrDownlinkErrorUrc.DL_DLER -> "Downlink (syntax) error"
                PskOrDownlinkErrorUrc.DL_ERR -> "Error processing (downlink) value"
                PskOrDownlinkErrorUrc.DL_HSER -> "SHA256 hash error"
                PskOrDownlinkErrorUrc.DL_CSER -> "Checksum error"
                null -> "Unknown URC"
            }
        logger.warn { "PSK set failed for device with id ${identity}: $errorMessage" }
        pskService.setPendingKeyAsInvalid(identity)
    }

    private fun urcsContainPskSuccess(urcs: List<String>) =
        urcs.any { urc -> urc.contains(URC_PSK_SUCCESS) }

    private fun handlePskSuccess(identity: String) {
        if (!pskService.isPendingKeyPresent(identity)) {
            throw NoExistingPskException(
                "Success URC received, but no pending key present to set as active")
        }
        logger.info { "PSK set successfully, changing active key" }
        pskService.changeActiveKey(identity)
    }
}
