// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.dto

import java.util.UUID
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket

data class FirmwareDTO(
    val id: UUID,
    val name: String,
    val version: String,
    val previousFirmwareId: UUID?,
    val packets: List<FirmwarePacket>
)
