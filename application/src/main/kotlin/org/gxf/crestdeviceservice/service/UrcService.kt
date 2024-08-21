// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.command.service.CommandService
import org.gxf.crestdeviceservice.model.ErrorUrc
import org.gxf.crestdeviceservice.model.ErrorUrc.Companion.isErrorUrc
import org.gxf.crestdeviceservice.model.ErrorUrc.Companion.isPskErrorUrc
import org.gxf.crestdeviceservice.psk.exception.NoExistingPskException
import org.gxf.crestdeviceservice.psk.service.PskService
import org.springframework.stereotype.Service

@Service
class UrcService(
    private val pskService: PskService,
    private val commandService: CommandService
) {
    companion object {
        private const val URC_FIELD = "URC"
        private const val URC_PSK_SUCCESS = "PSK:SET"
    }

    private val logger = KotlinLogging.logger {}

    fun interpretURCInMessage(identity: String, body: JsonNode) {
        val urcs = getUrcsFromMessage(body)
        if (urcs.isEmpty()) {
            logger.debug { "Received message without urcs" }
            return
        }
        logger.debug { "Received message with urcs ${urcs.joinToString(", ")}" }

        if (urcsContainPskError(urcs)) {
            handlePskErrors(identity, urcs) // todo in psk service?
            return
        } else if (urcsContainPskSuccess(urcs)) {
            handlePskSuccess(identity)
            return
        }

        val pendingCommand = commandService.getFirstPendingCommandForDevice(identity) // todo moet dit niet in progress zijn?
        if (pendingCommand != null) {
            // Handle downlinks for pending command
            if (urcsContainErrors(urcs)) { // todo gaat dit per se over gestuurde command?
                commandService.handleCommandError(identity, pendingCommand)
            } else { // todo is geen error automatisch een succes? nee, kan urc over iets anders zijn
                commandService.handleCommandSuccess(identity, pendingCommand)
            }
        }
    }

    // PSK
    private fun getUrcsFromMessage(body: JsonNode) =
        body[URC_FIELD].filter { it.isTextual }.map { it.asText() }

    private fun urcsContainPskError(urcs: List<String>) =
        urcs.any { urc -> isPskErrorUrc(urc) }

    private fun handlePskErrors(identity: String, urcs: List<String>) {
        if (!pskService.isPendingKeyPresent(identity)) {
            throw NoExistingPskException(
                "Failure URC received, but no pending key present to set as invalid"
            )
        }

        urcs.filter { urc -> isPskErrorUrc(urc) }
            .forEach { urc ->
                logger.warn {
                    "PSK set failed for device with id ${identity}: ${ErrorUrc.messageFromCode(urc)}"
                }
            }

        pskService.setPendingKeyAsInvalid(identity)
    }

    private fun urcsContainPskSuccess(urcs: List<String>) =
        urcs.any { urc -> urc.contains(URC_PSK_SUCCESS) }

    private fun handlePskSuccess(identity: String) {
        if (!pskService.isPendingKeyPresent(identity)) {
            throw NoExistingPskException(
                "Success URC received, but no pending key present to set as active"
            )
        }
        logger.info { "PSK set successfully, changing active key" }
        pskService.changeActiveKey(identity)
    }

    // Other errors
    private fun urcsContainErrors(urcs: List<String>) =
        urcs.any { urc -> isErrorUrc(urc) }

}
