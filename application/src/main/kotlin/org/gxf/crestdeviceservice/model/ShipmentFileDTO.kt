// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class ShipmentFileDTO(val shipment: Shipment, val devices: List<Device>) {
    @JsonIgnoreProperties(ignoreUnknown = true) data class Shipment(val version: String, val shipmentNumber: String)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Device(val rtu: Rtu) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Rtu(
            val rtuId: String,
            val pskEncrypted: String,
            val pskChangeSecretEncrypted: String,
            val pskEncryptionKeyRef: String,
        )
    }
}
