// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.IntegrationTestHelper.createKafkaConsumer
import org.gxf.crestdeviceservice.IntegrationTestHelper.getFileContentAsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(
        topics = ["\${crest-device-service.kafka.message-producer.topic-name}"],
)
class MessageHandelingTest {

    @Value("\${crest-device-service.kafka.message-producer.topic-name}")
    private lateinit var crestMessageTopicName: String

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    @Test
    fun shouldProduceMessageForValidRequest() {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity<String>(getFileContentAsString("message.json"), headers)

        val consumer = createKafkaConsumer(embeddedKafkaBroker, crestMessageTopicName)
        val response = testRestTemplate.postForEntity("/sng/1", request, String::class.java)

        assertThat("0").isEqualTo(response.body)

        val records = consumer.poll(Duration.ofSeconds(1))

        assertThat(records.records(crestMessageTopicName)).hasSize(1)


        val expectedJsonNode = ObjectMapper().readTree(getFileContentAsString("message.json"))
        val payloadJsonNode = ObjectMapper().readTree(records.records(crestMessageTopicName).first().value().payload)

        assertThat(expectedJsonNode).isEqualTo(payloadJsonNode)
    }
}
