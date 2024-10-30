// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import java.util.UUID
import org.apache.avro.specific.SpecificRecordBase
import org.gxf.crestdeviceservice.FirmwaresFactory
import org.gxf.crestdeviceservice.TestConstants.COMMAND_FEEDBACK_TOPIC
import org.gxf.crestdeviceservice.TestConstants.DEVICE_MESSAGE_TOPIC
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_KEY
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_TOPIC
import org.gxf.crestdeviceservice.config.KafkaProducerProperties
import org.gxf.crestdeviceservice.config.KafkaProducerTopicKeyProperties
import org.gxf.crestdeviceservice.config.KafkaProducerTopicProperties
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.mapper.FirmwareMapper
import org.gxf.crestdeviceservice.firmware.service.FirmwareService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate

@ExtendWith(MockitoExtension::class)
class FirmwareProducerServiceTest {
    @Mock private lateinit var mockedKafkaTemplate: KafkaTemplate<String, SpecificRecordBase>
    @Mock private lateinit var firmwareSerivce: FirmwareService
    @Mock private lateinit var firmwareMapper: FirmwareMapper

    private val kafkaProducerProperties =
        KafkaProducerProperties(
            KafkaProducerTopicProperties(DEVICE_MESSAGE_TOPIC),
            KafkaProducerTopicProperties(COMMAND_FEEDBACK_TOPIC),
            KafkaProducerTopicKeyProperties(FIRMWARE_TOPIC, FIRMWARE_KEY)
        )

    @Test
    fun shouldCallMessageProducerWithCorrectParams() {
        val firmwareProducerService =
            FirmwareProducerService(mockedKafkaTemplate, kafkaProducerProperties, firmwareSerivce, firmwareMapper)
        val firmwareEntityList =
            listOf(Firmware(UUID.randomUUID(), "fw1", "1"), Firmware(UUID.randomUUID(), "fw2", "1"))
        val avroFirmwares = FirmwaresFactory.getFirmwares()

        whenever(firmwareSerivce.findAllFirmwares()).thenReturn(firmwareEntityList)
        whenever(firmwareMapper.mapEntitiesToFirmwares(firmwareEntityList)).thenReturn(avroFirmwares)

        firmwareProducerService.sendAllFirmwares()

        verify(mockedKafkaTemplate).send(FIRMWARE_TOPIC, FIRMWARE_KEY, avroFirmwares)
    }
}
