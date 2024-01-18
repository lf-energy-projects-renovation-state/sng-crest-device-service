package org.gxf.crestdeviceservice.psk

import org.junit.jupiter.api.Test
import java.io.File
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.createParentDirectories


class PSKKeyGenerator {

    @Test
    fun generateKeyPair() {
        val generator: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
            .apply { initialize(4096) }
        val pair: KeyPair = generator.generateKeyPair()
        Path("/tmp/keys")
        File("/tmp/keys/private.key")
            .apply { toPath().createParentDirectories() }
            .apply { createNewFile() }
            .writeText(privateKeyToString(pair.private))
        File("/tmp/keys/public.key")
            .apply { toPath().createParentDirectories() }
            .apply { createNewFile() }
            .writeText(publicKeyToString(pair.public))
    }

    private fun publicKeyToString(publicKey: PublicKey) =
        Base64.getEncoder().encodeToString(publicKey.encoded)
            .chunked(32)
            .joinToString("\n", prefix = "-----BEGIN CERTIFICATE-----\n", postfix = "\n-----END CERTIFICATE-----")


    private fun privateKeyToString(privateKey: PrivateKey) =
        Base64.getEncoder().encodeToString(privateKey.encoded)
            .chunked(32)
            .joinToString("\n", prefix = "-----BEGIN PRIVATE KEY-----\n", postfix = "\n-----END PRIVATE KEY-----")

}
