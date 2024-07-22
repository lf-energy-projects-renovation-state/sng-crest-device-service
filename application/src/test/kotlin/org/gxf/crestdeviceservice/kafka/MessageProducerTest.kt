// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.avro.specific.SpecificRecordBase
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.kafka.configuration.KafkaProducerProperties
import org.gxf.sng.avro.DeviceMessage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.check
import org.springframework.kafka.core.KafkaTemplate

@ExtendWith(MockitoExtension::class)
class MessageProducerTest {

    @Mock private lateinit var mockedKafkaTemplate: KafkaTemplate<String, SpecificRecordBase>

    private val kafkaProducerProperties = KafkaProducerProperties("topic")

    @Test
    fun shouldCallMessageProducerWithCorrectParams() {
        val messageProducer = MessageProducer(mockedKafkaTemplate, kafkaProducerProperties)

        val jsonNode =
            ObjectMapper()
                .readTree("""
            {
                "ID":12345
            }
        """)
        messageProducer.produceMessage(jsonNode)
        verify(mockedKafkaTemplate)
            .send(
                check { assertThat(it).isEqualTo("topic") },
                check { assertThat((it as DeviceMessage).payload).isEqualTo(jsonNode.toString()) })
    }
}
