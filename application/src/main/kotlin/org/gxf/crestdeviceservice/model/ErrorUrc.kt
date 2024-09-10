// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.model

enum class ErrorUrc(val message: String) {
    UNK("Downlink unknown"),
    EQER("Set PSK does not equal earlier PSK"),
    DLNA("Downlink not allowed"),
    DLER("Downlink (syntax) error"),
    HSER("SHA256 hash error"),
    CSER("Checksum error"),
    ERR("Error processing (downlink) value");

    companion object {
        fun getMessageFromCode(code: String): String {
            val error = entries.firstOrNull { code.endsWith(it.name) }
            return error?.message ?: "Unknown URC"
        }
    }
}
