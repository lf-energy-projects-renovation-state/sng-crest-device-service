// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.service

import org.gxf.crestdeviceservice.firmware.dto.FirmwareDTO
import org.gxf.crestdeviceservice.firmware.mapper.FirmwareMapper
import org.gxf.crestdeviceservice.firmware.repository.FirmwarePacketRepository
import org.gxf.crestdeviceservice.firmware.repository.FirmwareRepository
import org.springframework.stereotype.Service

@Service
class FirmwareService(
    private val firmwareRepository: FirmwareRepository,
    private val firmwarePacketRepository: FirmwarePacketRepository,
    private val firmwareMapper: FirmwareMapper
) {
    fun processFirmware(firmwareDTO: FirmwareDTO) {
        val firmware = firmwareMapper.mapFirmwareDTOToEntity(firmwareDTO)
        firmwareRepository.save(firmware)
        firmware.packets.forEach { packet -> firmwarePacketRepository.save(packet) }
        // todo send to maki
    }
}
