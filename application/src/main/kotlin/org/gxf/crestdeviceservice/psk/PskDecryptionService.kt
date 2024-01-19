package org.gxf.crestdeviceservice.psk

import org.gxf.crestdeviceservice.psk.configuration.PskDecryptionConfiguration
import org.gxf.crestdeviceservice.psk.exception.UnknownKeyRefException
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.Cipher


@Service
class PskDecryptionService(private val pskDecryptionConfiguration: PskDecryptionConfiguration) {

    fun decryptSecret(encryptedSecret: String, keyRef: String): String {
        val decodedSecret: ByteArray = Base64.getDecoder().decode(encryptedSecret)

        return cypherForKeyRef(keyRef)
            .doFinal(decodedSecret)
            .decodeToString()
    }

    private fun cypherForKeyRef(keyRef: String): Cipher {
        val publicKey = pskDecryptionConfiguration.publicKey[keyRef]
            ?: throw UnknownKeyRefException("Keyref not found in configuration")

        return Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING")
            .apply { init(Cipher.DECRYPT_MODE, publicKey) }
    }
}
