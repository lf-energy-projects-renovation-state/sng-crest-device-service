package org.gxf.crestdeviceservice.data.convertors

import jakarta.persistence.AttributeConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Component
class DatabaseFieldEncryptor : AttributeConverter<String, String> {

    companion object {
        private const val ENCRYPTION_METHOD = "AES/CBC/PKCS5Padding"
        private const val ENCRYPTION_ALGORITHM = "AES"
        private const val IV_SIZE = 16
    }

    @Value("\${crest-device-service.database.encryption-key}")
    lateinit var secret: String

    override fun convertToDatabaseColumn(attribute: String): String {
        return Base64.getEncoder().encodeToString(encrypt(attribute, secret))
    }

    override fun convertToEntityAttribute(dbData: String): String {
        return decrypt(Base64.getDecoder().decode(dbData), secret)
    }

    fun encrypt(plainText: String, key: String): ByteArray {
        val inputDataAsBytes = plainText.toByteArray()

        // Generating IV
        val iv = ByteArray(IV_SIZE).apply {
            SecureRandom().nextBytes(this)
        }
        val ivParameterSpec = IvParameterSpec(iv)

        // Hashing key
        val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
        digest.update(key.toByteArray(charset("UTF-8")))
        val keyBytes = ByteArray(IV_SIZE)
        System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.size)
        val secretKeySpec = SecretKeySpec(keyBytes, ENCRYPTION_ALGORITHM)

        // Encrypt
        val cipher = Cipher.getInstance(ENCRYPTION_METHOD)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        val encrypted = cipher.doFinal(inputDataAsBytes)

        // Combine IV and encrypted part
        val encryptedIVAndText = ByteArray(IV_SIZE + encrypted.size)
        System.arraycopy(iv, 0, encryptedIVAndText, 0, IV_SIZE)
        System.arraycopy(encrypted, 0, encryptedIVAndText, IV_SIZE, encrypted.size)
        return encryptedIVAndText
    }

    fun decrypt(encryptedIvTextBytes: ByteArray, key: String): String {
        val ivSize = IV_SIZE
        val keySize = secret.length

        // Extract IV
        val iv = ByteArray(ivSize)
        System.arraycopy(encryptedIvTextBytes, 0, iv, 0, iv.size)
        val ivParameterSpec = IvParameterSpec(iv)

        // Extract encrypted part
        val encryptedSize = encryptedIvTextBytes.size - ivSize
        val encryptedBytes = ByteArray(encryptedSize)
        System.arraycopy(encryptedIvTextBytes, ivSize, encryptedBytes, 0, encryptedSize)

        // Hash key
        val keyBytes = ByteArray(keySize)
        val md: MessageDigest = MessageDigest.getInstance("SHA-256")
        md.update(key.toByteArray())
        System.arraycopy(md.digest(), 0, keyBytes, 0, keyBytes.size)
        val secretKeySpec = SecretKeySpec(keyBytes, ENCRYPTION_ALGORITHM)

        // Decrypt
        val cipherDecrypt = Cipher.getInstance(ENCRYPTION_METHOD)
        cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        val decrypted = cipherDecrypt.doFinal(encryptedBytes)
        return String(decrypted)
    }
}
