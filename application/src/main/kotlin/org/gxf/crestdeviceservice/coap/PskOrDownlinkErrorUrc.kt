/*
 * SPDX-FileCopyrightText: Contributors to the GXF project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.gxf.crestdeviceservice.coap

enum class PskOrDownlinkErrorUrc(val code: String) {
    PSK_EQER("PSK:EQER"),
    DL_UNK("DL:UNK"),
    DL_DLNA("[DL]:DLNA"),
    DL_DLER("[DL]:DLER"),
    DL_ERR("[DL]:#ERR"),
    DL_HSER("[DL]:HSER"),
    DL_CSER("[DL]:CSER");

    companion object {
        fun from(code: String): PskOrDownlinkErrorUrc? = entries.firstOrNull { it.code == code }
    }
}
