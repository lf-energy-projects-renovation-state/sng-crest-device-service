// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.service

import com.alliander.sng.Firmwares
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.exception.FirmwareException
import org.gxf.crestdeviceservice.firmware.mapper.FirmwareMapper
import org.gxf.crestdeviceservice.firmware.repository.FirmwareRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FirmwareService(
    private val firmwareRepository: FirmwareRepository,
    private val firmwareMapper: FirmwareMapper,
) {
    private val logger = KotlinLogging.logger {}

    fun processFirmware(file: MultipartFile): Firmwares {
        val firmware = firmwareMapper.mapFirmwareFileToEntity(file)
        if (firmwareRepository.findByName(firmware.name) != null) {
            throw FirmwareException("Firmware with name ${firmware.name} already exists")
        }
        save(firmware)
        val firmwareEntities: List<Firmware> = firmwareRepository.findAll()
        return firmwareMapper.mapEntitiesToFirmwares(firmwareEntities)
    }

    private fun save(firmware: Firmware) {
        logger.info { "Saving firmware with name ${firmware.name} to database" }
        firmwareRepository.save(firmware)
    }
}
