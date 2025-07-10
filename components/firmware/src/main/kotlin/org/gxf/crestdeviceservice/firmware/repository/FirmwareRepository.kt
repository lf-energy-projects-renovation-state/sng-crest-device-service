// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.repository

import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FirmwareRepository : CrudRepository<Firmware, UUID> {
    fun findByName(name: String): Firmware?

    override fun findAll(): List<Firmware>
}
