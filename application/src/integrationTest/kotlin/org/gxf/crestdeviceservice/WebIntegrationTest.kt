// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.annotation.DirtiesContext
import org.springframework.util.LinkedMultiValueMap

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext
class WebIntegrationTest {
    @Autowired lateinit var restTemplate: TestRestTemplate

    @Test
    fun firmwareFileUploadTest() {
        // arrange
        val testFile = ClassPathResource("RTU#FULL#TO#23.10.txt").file

        // act
        val response = uploadFile(testFile)

        // assert
        assertThat(response.statusCode.value()).isEqualTo(302)
    }

    fun uploadFile(file: File): ResponseEntity<String> {
        val headers: HttpHeaders =
            HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }

        val body: LinkedMultiValueMap<String, Any> =
            LinkedMultiValueMap<String, Any>().apply { add("file", FileSystemResource(file)) }
        val requestEntity = HttpEntity(body, headers)

        return this.restTemplate.postForEntity<String>("/web/firmware", requestEntity)
    }
}
