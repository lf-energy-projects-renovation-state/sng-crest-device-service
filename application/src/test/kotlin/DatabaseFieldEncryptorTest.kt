import org.gxf.crestdeviceservice.data.convertors.DatabaseFieldEncryptor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DatabaseFieldEncryptorTest {

    private val databaseFieldEncryptor = DatabaseFieldEncryptor().apply { secret = "super-secret-key" }

    @Test
    fun shouldEncryptInputData() {
        val expected = "pq+bgx2cDcgGCYikWQnJ7g=="
        val result = databaseFieldEncryptor.convertToDatabaseColumn("data")

        assertEquals(expected, result)
    }

    @Test
    fun shouldDecryptData() {
        val expected = "data"
        val result = databaseFieldEncryptor.convertToEntityAttribute("pq+bgx2cDcgGCYikWQnJ7g==")

        assertEquals(expected, result)
    }
}
