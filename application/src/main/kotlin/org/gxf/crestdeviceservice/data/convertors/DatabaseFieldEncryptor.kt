package org.gxf.crestdeviceservice.data.convertors

import jakarta.persistence.AttributeConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@Component
class DatabaseFieldEncryptor : AttributeConverter<String, String> {

    companion object {
        private const val ENCRYPTION_METHOD = "AES"
    }

    @Value("\${crest-device-service.database.encryption-key}")
    lateinit var secret: String

    override fun convertToDatabaseColumn(attribute: String): String {
        val cipher = Cipher.getInstance(ENCRYPTION_METHOD).apply {
            init(Cipher.ENCRYPT_MODE, SecretKeySpec(secret.toByteArray(), ENCRYPTION_METHOD))
        }

        return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.toByteArray()))
    }

    override fun convertToEntityAttribute(dbData: String): String {
        val cipher = Cipher.getInstance(ENCRYPTION_METHOD).apply {
            init(Cipher.DECRYPT_MODE, SecretKeySpec(secret.toByteArray(), ENCRYPTION_METHOD))
        }

        return String(cipher.doFinal(Base64.getDecoder().decode(dbData)))
    }
}
