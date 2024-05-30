// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.coap

enum class PskOrDownlinkErrorUrc(val code: String, val message: String) {
    PSK_EQER("PSK:EQER", "Set PSK does not equal earlier PSK"),
    DL_UNK("DL:UNK", "Downlink unknown"),
    DL_DLNA("[DL]:DLNA", "Downlink not allowed"),
    DL_DLER("[DL]:DLER", "Downlink (syntax) error"),
    DL_ERR("[DL]:#ERR", "Error processing (downlink) value"),
    DL_HSER("[DL]:HSER", "SHA256 hash error"),
    DL_CSER("[DL]:CSER", "Checksum error");

    companion object {
        fun messageFromCode(code: String): String {
            val error = entries.firstOrNull { it.code == code }
            return error?.message ?: "Unknown URC"
        }

        fun isPskOrDownlinkErrorURC(code: String) = entries.any { it.code == code }
    }
}
