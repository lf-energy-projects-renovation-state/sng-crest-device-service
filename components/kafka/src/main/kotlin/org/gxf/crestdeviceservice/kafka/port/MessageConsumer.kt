// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.kafka.port

interface MessageConsumer<T> {
    fun consumeMessage(message: T)
}