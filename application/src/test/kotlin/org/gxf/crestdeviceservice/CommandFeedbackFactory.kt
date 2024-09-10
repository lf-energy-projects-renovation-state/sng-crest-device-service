// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import com.alliander.sng.CommandFeedback
import com.alliander.sng.CommandStatus
import org.gxf.crestdeviceservice.TestConstants.CORRELATION_ID
import org.gxf.crestdeviceservice.TestConstants.DEVICE_ID
import org.gxf.crestdeviceservice.TestConstants.MESSAGE_RECEIVED
import org.gxf.crestdeviceservice.TestConstants.timestamp

object CommandFeedbackFactory {
    fun rebootCommandReceivedFeedback() =
        CommandFeedback.newBuilder()
            .setDeviceId(DEVICE_ID)
            .setCorrelationId(CORRELATION_ID)
            .setTimestampStatus(timestamp)
            .setStatus(CommandStatus.Received)
            .setMessage(MESSAGE_RECEIVED)
            .build()
}
