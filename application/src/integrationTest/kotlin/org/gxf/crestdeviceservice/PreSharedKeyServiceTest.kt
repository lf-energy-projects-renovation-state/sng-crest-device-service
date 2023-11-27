package org.gxf.crestdeviceservice

import org.gxf.crestdeviceservice.data.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.PskRepository
import org.gxf.crestdeviceservice.psk.PskService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(
        topics = ["\${crest-device-service.kafka.message-producer.topic-name}"],
)
class PreSharedKeyServiceTest {

    companion object {
        private const val IDENTITY = "identity"
    }

    @Autowired
    private lateinit var pskService: PskService

    @Autowired
    private lateinit var pskRepository: PskRepository

    @BeforeEach
    fun setup() {
        pskRepository.deleteAll()
        pskRepository.save(PreSharedKey(IDENTITY, Instant.MIN, "123"))
    }

    @Test
    fun shouldRetrieveLatestPskWhenThereAreMultiple() {
        val expectedKey = "1234"
        pskRepository.save(PreSharedKey(IDENTITY, Instant.MAX, expectedKey))

        val currentPsk = pskService.getCurrentPsk(IDENTITY)

        assertEquals(expectedKey, currentPsk)
    }

    @Test
    fun shouldCreateAndSaveNewPsk() {
        val newKey = pskService.generateAndSetNewKeyForIdentity(IDENTITY)

        val savedKey = pskRepository.findFirstByIdentityOrderByRevisionTimeDesc(IDENTITY)

        // There should be a key in the database
        assertTrue(savedKey != null)

        // New key should only contain alphanumerical chars and should be 16 chars long
        assertTrue(newKey.matches("^[a-zA-Z0-9]*$".toRegex()) && newKey.length == 16)
    }

    @Test
    fun hasDefaultKeyShouldReturnTrueWhenThereIsOneKeyForIdentity() {
        val result = pskService.hasDefaultKey(IDENTITY)

        assertTrue(result)
    }

    @Test
    fun hasDefaultKeyShouldReturnFalseWhenThereAreMoreThanOneKeyForIdentity() {
        pskRepository.save(PreSharedKey(IDENTITY, Instant.now(), "123"))

        val result = pskService.hasDefaultKey(IDENTITY)

        assertFalse(result)
    }

}
