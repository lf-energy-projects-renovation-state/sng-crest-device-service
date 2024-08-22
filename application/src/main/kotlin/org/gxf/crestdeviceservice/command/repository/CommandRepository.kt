// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.repository

import java.util.UUID
import org.gxf.crestdeviceservice.command.entity.Command
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CommandRepository : CrudRepository<Command, UUID> {
    fun findFirstByDeviceIdAndTypeOrderByTimestampIssuedDesc(
        deviceId: String,
        type: Command.CommandType
    ): Command?

    fun findFirstByDeviceIdAndStatusOrderByTimestampIssuedAsc(
        deviceId: String,
        status: Command.CommandStatus
    ): Command?
}
