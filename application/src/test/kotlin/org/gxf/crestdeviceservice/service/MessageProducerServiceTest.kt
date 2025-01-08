// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.apache.avro.specific.SpecificRecordBase
import org.gxf.crestdeviceservice.TestConstants.COMMAND_FEEDBACK_TOPIC
import org.gxf.crestdeviceservice.TestConstants.DEVICE_MESSAGE_TOPIC
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_KEY
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_TOPIC
import org.gxf.crestdeviceservice.config.KafkaProducerProperties
import org.gxf.crestdeviceservice.config.KafkaProducerTopicKeyProperties
import org.gxf.crestdeviceservice.config.KafkaProducerTopicProperties
import org.gxf.sng.avro.DeviceMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.kafka.core.KafkaTemplate

@ExtendWith(MockKExtension::class)
class MessageProducerServiceTest {
    @MockK(relaxed = true) private lateinit var kafkaTemplate: KafkaTemplate<String, SpecificRecordBase>
    private lateinit var kafkaProducerProperties: KafkaProducerProperties

    private lateinit var service: MessageProducerService

    @BeforeEach
    fun createService() {
        kafkaProducerProperties =
            KafkaProducerProperties(
                KafkaProducerTopicProperties(DEVICE_MESSAGE_TOPIC),
                KafkaProducerTopicProperties(COMMAND_FEEDBACK_TOPIC),
                KafkaProducerTopicKeyProperties(FIRMWARE_TOPIC, FIRMWARE_KEY),
            )

        service = MessageProducerService(kafkaTemplate, kafkaProducerProperties)
    }

    @Test
    fun shouldCallMessageProducerWithCorrectParams() {
        val jsonNode =
            ObjectMapper()
                .readTree(
                    """
                        {
                            "ID": 12345
                        }
                    """
                )

        service.produceMessage(jsonNode)

        verify {
            kafkaTemplate.send(
                DEVICE_MESSAGE_TOPIC,
                match { (it is DeviceMessage) && (it.payload == jsonNode.toString()) },
            )
        }
    }
}
