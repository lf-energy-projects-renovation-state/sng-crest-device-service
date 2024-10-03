// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.dto

import org.gxf.crestdeviceservice.firmware.entity.Firmware

data class FirmwarePacketDTO(val firmware: Firmware, val packetNumber: Int, val packet: String)
