// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.IntegrationTestHelper.getFileContentAsString
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PskRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(
        topics = ["\${crest-device-service.kafka.message-producer.topic-name}"],
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CoapMessageHandlingTest {

    companion object {
        private const val IDENTITY = "1234"
        private const val PRE_SHARED_KEY = "1234567890123456"
        private const val SECRET = "123456789"
    }

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var pskRepository: PskRepository

    @BeforeEach
    fun setup() {
        pskRepository.save(PreSharedKey(IDENTITY, Instant.MIN, PRE_SHARED_KEY, SECRET))
    }

    @AfterEach
    fun cleanup() {
        pskRepository.deleteAll()
    }

    @Test
    fun shouldReturnADownLinkContainingAPskSetCommandWhenTheKeyHasNotChangedYet() {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity<String>(getFileContentAsString("message.json"), headers)

        val result = restTemplate.postForEntity<String>("/sng/${IDENTITY}", request)

        assertThat(result.body).contains("PSK:", "SET")
    }

    @Test
    fun shouldNotReturnADownLinkContainingAPskSetCommandWhenTheKeyHasNotChangedYet() {
        // Set second PreSharedKey for device
        pskRepository.save(PreSharedKey(IDENTITY, Instant.now(), PRE_SHARED_KEY, SECRET))

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity<String>(getFileContentAsString("message.json"), headers)

        val result = restTemplate.postForEntity<String>("/sng/${IDENTITY}", request)

        assertThat(result.body).isEqualTo("0")
    }
}
