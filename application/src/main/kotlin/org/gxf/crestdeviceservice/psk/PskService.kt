package org.gxf.crestdeviceservice.psk

import org.gxf.crestdeviceservice.data.entity.Psk
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Instant

@Service
class PskService(private val pskRepository: PskRepository) {

    companion object {
        private const val KEY_LENGTH = 16L
    }

    fun getCurrentPsk(identity: String) =
            pskRepository.findFirstByIdentityOrderByRevisionTimeStampDesc(identity)?.key

    fun generateAndSetNewKeyForIdentity(identity: String): String {
        val newKey = generatePsk()
        pskRepository.save(Psk(identity, Instant.now(), newKey))
        return newKey
    }

    fun hasDefaultKey(identity: String): Boolean {
        return pskRepository.countPsksByIdentity(identity) == 1L
    }

    private fun generatePsk(): String {
        val allowedChars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val secureRandom = SecureRandom.getInstanceStrong()

        return secureRandom.ints(KEY_LENGTH, 0, allowedChars.length).toArray().fold("") { acc, next -> acc + allowedChars[next] }
    }
}
