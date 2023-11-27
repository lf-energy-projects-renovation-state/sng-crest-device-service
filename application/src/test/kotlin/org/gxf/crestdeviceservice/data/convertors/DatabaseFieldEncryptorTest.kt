package org.gxf.crestdeviceservice.data.convertors

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class DatabaseFieldEncryptorTest {

    private val databaseFieldEncryptor = DatabaseFieldEncryptor().apply { secret = "super-secret-key" }

    @Test
    fun shouldBeAbleTeEncryptAndDecryptData() {
        val expected = "data"
        val encrypted = databaseFieldEncryptor.convertToDatabaseColumn(expected)
        val decrypted = databaseFieldEncryptor.convertToEntityAttribute(encrypted)

        assertNotEquals("data", encrypted)
        assertEquals(expected, decrypted)
    }
}
