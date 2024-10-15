// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.service

import org.gxf.crestdeviceservice.firmware.mapper.FirmwareMapper
import org.gxf.crestdeviceservice.firmware.repository.FirmwarePacketRepository
import org.gxf.crestdeviceservice.firmware.repository.FirmwareRepository
import org.mockito.kotlin.mock

class FirmwareServiceTest {
    private val firmwareRepository = mock<FirmwareRepository>()
    private val firmwarePacketRepository = mock<FirmwarePacketRepository>()
    private val firmwareMapper = mock<FirmwareMapper>()
    private val firmwareService = FirmwareService(firmwareRepository, firmwarePacketRepository, firmwareMapper)
}
