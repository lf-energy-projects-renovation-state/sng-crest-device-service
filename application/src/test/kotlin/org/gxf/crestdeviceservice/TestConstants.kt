// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import java.time.Instant
import java.util.UUID

object TestConstants {
    const val DEVICE_ID = "device-id"
    const val MESSAGE_RECEIVED = "Command received"
    val CORRELATION_ID = UUID.randomUUID()
    val timestamp = Instant.now()
}
