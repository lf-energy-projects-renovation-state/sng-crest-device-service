// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.model

import com.fasterxml.jackson.databind.JsonNode

class DeviceMessage(val message: JsonNode) {
    fun getFotaMessageCounter() = message[FMC_FIELD].intValue()

    fun getUrcContainingField(fieldName: String): JsonNode = message.path(URC_FIELD).findParent(fieldName)

    fun getDownlinkCommand() = message.path(URC_FIELD).findValue(DL_FIELD)?.asText()

    companion object {
        const val FMC_FIELD = "FMC"
        const val URC_FIELD = "URC"
        const val DL_FIELD = "DL"
    }
}
