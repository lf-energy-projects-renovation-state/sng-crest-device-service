package org.gxf.crestdeviceservice.psk

import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PskRepository
import org.gxf.crestdeviceservice.psk.exception.InitialKeySetException
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Instant

@Service
class PskService(private val pskRepository: PskRepository, private val pskConfiguration: PskConfiguration) {

    companion object {
        private const val KEY_LENGTH = 16L
        private const val ALLOWED_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    }

    fun getCurrentPsk(identity: String) =
        pskRepository.findFirstByIdentityOrderByRevisionDesc(identity)?.preSharedKey

    fun setInitialKeyForIdentify(identity: String, psk: String, secret: String) {
        if (pskRepository.countPsksByIdentity(identity) != 0L) {
            throw InitialKeySetException("Key already exists for identity. Key cannot be overridden")
        }
        pskRepository.save(PreSharedKey(identity, 0, Instant.now(), psk, secret))
    }

    fun generateAndSetNewKeyForIdentity(identity: String): PreSharedKey {
        val newKey = generatePsk()
        val previousPSK = pskRepository.findFirstByIdentityOrderByRevisionDesc(identity)!!
        val newVersion = previousPSK.revision + 1
        return pskRepository.save(PreSharedKey(identity, newVersion, Instant.now(), newKey, previousPSK.secret))
    }

    fun needsKeyChange(identity: String) =
        pskConfiguration.changeInitialPsk &&
                pskRepository.countPsksByIdentity(identity) == 1L


    private fun generatePsk(): String {
        val secureRandom = SecureRandom.getInstanceStrong()

        return secureRandom.ints(KEY_LENGTH, 0, ALLOWED_CHARACTERS.length).toArray()
            .fold("") { acc, next -> acc + ALLOWED_CHARACTERS[next] }
    }
}
