package org.gxf.crestdeviceservice.data.convertors

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DatabaseFieldEncryptorTest {

    private val databaseFieldEncryptor = DatabaseFieldEncryptor().apply { secret = "super-secret-key" }

    @Test
    fun shouldBeAbleTeEncryptAndDecryptData() {
        val expected = "data"
        val encrypted = databaseFieldEncryptor.convertToDatabaseColumn(expected)
        val decrypted = databaseFieldEncryptor.convertToEntityAttribute(encrypted)

        assertThat("data").isNotEqualTo(encrypted)
        assertThat(expected).isEqualTo(decrypted)
    }
}
