// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.entity

import java.io.Serializable

data class FirmwarePacketCompositeKey(val firmware: Firmware, val packetNumber: Int) : Serializable
