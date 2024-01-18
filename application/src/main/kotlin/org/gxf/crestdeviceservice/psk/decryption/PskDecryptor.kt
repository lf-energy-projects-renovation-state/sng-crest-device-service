package org.gxf.crestdeviceservice.psk.decryption

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.Cipher


@Service
class PskDecryptor(private val pskDecryptionConfiguration: PskDecryptionConfiguration) {

    @Scheduled
    fun decryptSecret(encryptedSecret: String): String {
        val decodedSecret: ByteArray = Base64.getDecoder().decode(encryptedSecret)

        val decryptedBytes: ByteArray = Cipher.getInstance("RSA")
            .apply { init(Cipher.DECRYPT_MODE, pskDecryptionConfiguration.publicKey) }
            .run { doFinal(decodedSecret) }

        return decryptedBytes.decodeToString()
    }
}
