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
    const val FIRMWARE_VERSION = "99.99"
    const val FIRMWARE_FROM_VERSION = "23.10"
    const val FIRMWARE_NAME = "RTU#DELTA#FROM#$FIRMWARE_FROM_VERSION#TO#$FIRMWARE_VERSION"
    const val NUMBER_OF_PACKETS = 13
    const val DEVICE_MESSAGE_TOPIC = "device-message"
    const val COMMAND_FEEDBACK_TOPIC = "command-feedback"
    const val FIRMWARE_TOPIC = "firmware-topic"
}
