package org.gxf.crestdeviceservice

import org.gxf.crestdeviceservice.IntegrationTestHelper.createKafkaConsumer
import org.gxf.crestdeviceservice.IntegrationTestHelper.getFileContentAsString
import org.junit.jupiter.api.Assertions
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
    fun produceMessageTest() {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request: HttpEntity<String> = HttpEntity<String>(getFileContentAsString("message.json"), headers)

        val consumer = createKafkaConsumer(embeddedKafkaBroker, crestMessageTopicName)
        val response = testRestTemplate.postForEntity("/sng/1", request, String::class.java)

        Assertions.assertEquals("0", response.body)

        val records = consumer.poll(Duration.ofSeconds(1))

        Assertions.assertEquals(1, records.records(crestMessageTopicName).count())

        val expectedJsonNode = ObjectMapper().readTree(getFileContentAsString("message.json"))
        val payloadJsonNode = ObjectMapper().readTree(records.records(crestMessageTopicName).first().value().payload)

        Assertions.assertEquals(expectedJsonNode, payloadJsonNode)
    }
}
