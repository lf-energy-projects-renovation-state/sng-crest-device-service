import org.gxf.crestdeviceservice.data.convertors.DatabaseFieldEncryptor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DatabaseFieldEncryptorTest {

    private val databaseFieldEncryptor = DatabaseFieldEncryptor().apply { secret = "super-secret-key" }

    @Test
    fun shouldBeAbleTeEncryptAndDecryptData() {
        val expected = "data"
        val encrypted = databaseFieldEncryptor.convertToDatabaseColumn(expected)
        val decrypted = databaseFieldEncryptor.convertToEntityAttribute(encrypted)
        assertEquals(expected, decrypted)
    }
}
