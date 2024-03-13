package org.gxf.crestdeviceservice.psk

import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
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

    private val secureRandom: SecureRandom = SecureRandom.getInstanceStrong()

    fun getCurrentPsk(identity: String) =
        pskRepository.findLatestActivePsk(identity)?.preSharedKey

    fun setInitialKeyForIdentify(identity: String, psk: String, secret: String) {
        if (pskRepository.countActiveAndInactivePSKs(identity) != 0L) {
            throw InitialKeySetException("Key already exists for identity. Key cannot be overridden")
        }
        pskRepository.save(PreSharedKey(identity, 0, Instant.now(), psk, secret, PreSharedKeyStatus.ACTIVE))
    }

    fun generateAndSetNewKeyForIdentity(identity: String): PreSharedKey {
        val newKey = generatePsk()
        val previousPSK = pskRepository.findLatestActivePsk(identity)!!
        val newVersion = previousPSK.revision + 1
        return pskRepository.save(
            PreSharedKey(identity, newVersion, Instant.now(), newKey, previousPSK.secret, PreSharedKeyStatus.INACTIVE)
        )
    }

    fun needsKeyChange(identity: String): Boolean =
        pskConfiguration.changeInitialPsk &&
                (pskRepository.countActiveAndInactivePSKs(identity) == 1L ||
                        multiplePsksWithOldestPskActiveAndNoneInProgress(identity))

    fun multiplePsksWithOldestPskActiveAndNoneInProgress(identity: String) =
        pskRepository.countActiveAndInactivePSKs(identity) > 1L &&
                pskRepository.findOldestPsk(identity)?.status?.equals(PreSharedKeyStatus.ACTIVE) == true &&
                anyPsksHaveStatus(PreSharedKeyStatus.INACTIVE) &&
                !anyPsksHaveStatus(PreSharedKeyStatus.PENDING)

    fun anyPsksHaveStatus(status: PreSharedKeyStatus) =
        pskRepository.findAll().any { psk -> psk.status == status }

    fun setLastKeyAsActive(identity: String) =
        setStatus(identity, PreSharedKeyStatus.ACTIVE)

    fun setLastKeyAsFailed(identity: String) =
        setStatus(identity, PreSharedKeyStatus.FAILED)

    fun setStatus(identity: String, status: PreSharedKeyStatus) {
        val psk = pskRepository.findLatestPsk(identity)
            ?: TODO("create custom exception")
        psk.status = PreSharedKeyStatus.ACTIVE
        pskRepository.save(psk)
    }

    private fun generatePsk(): String =
        secureRandom.ints(KEY_LENGTH, 0, ALLOWED_CHARACTERS.length).toArray()
            .fold("") { acc, next -> acc + ALLOWED_CHARACTERS[next] }
}
