package org.gxf.crestdeviceservice.psk

import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyStatus
import org.gxf.crestdeviceservice.psk.entity.PskRepository
import org.gxf.crestdeviceservice.psk.exception.InitialKeySetException
import org.gxf.crestdeviceservice.psk.exception.NoExistingPskException
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

    fun getCurrentActiveKey(identity: String) =
        getCurrentActivePsk(identity)?.preSharedKey

    private fun getCurrentActivePsk(identity: String) =
        pskRepository.findLatestPskForIdentityWithStatus(
            identity,
            PreSharedKeyStatus.ACTIVE
        )

    fun pendingKeyPresent(identity: String) = getCurrentPendingKey(identity) != null

    private fun getCurrentPendingKey(identity: String) =
        pskRepository.findLatestPskForIdentityWithStatus(
            identity,
            PreSharedKeyStatus.PENDING
        )

    fun setInitialKeyForIdentity(identity: String, psk: String, secret: String) {
        if (pskRepository.countPSKsForIdWithStatus(identity, PreSharedKeyStatus.ACTIVE) != 0L) {
            throw InitialKeySetException("Key already exists for identity. Key cannot be overridden")
        }
        pskRepository.save(PreSharedKey(identity, 0, Instant.now(), psk, secret, PreSharedKeyStatus.ACTIVE))
    }

    fun generateNewReadyKeyForIdentity(identity: String) {
        val newKey = generatePsk()
        val previousPSK =
            pskRepository.findLatestPskForIdentityWithStatus(identity, PreSharedKeyStatus.ACTIVE)!!
        val newVersion = previousPSK.revision + 1
        pskRepository.save(
            PreSharedKey(
                identity,
                newVersion,
                Instant.now(),
                newKey,
                previousPSK.secret,
                PreSharedKeyStatus.READY
            )
        )
    }

    fun saveReadyKeyForIdentityAsPending(identity: String): PreSharedKey {
        val readyPsk =
            pskRepository.findLatestPskForIdentityWithStatus(identity, PreSharedKeyStatus.READY)!!
        readyPsk.status = PreSharedKeyStatus.PENDING
        return pskRepository.save(readyPsk)
    }

    fun needsKeyChange(identity: String) =
        changeInitialPsk() &&
                pskRepository.findLatestPskForIdentityWithStatus(
                    identity,
                    PreSharedKeyStatus.READY
                ) != null

    fun changeInitialPsk() = pskConfiguration.changeInitialPsk

    fun setPendingKeyAsInvalid(identity: String) {
        val psk = getCurrentPendingKey(identity)
            ?: throw NoExistingPskException("No pending key exists to set as invalid")
        psk.status = PreSharedKeyStatus.INVALID
        pskRepository.save(psk)
    }

    private fun generatePsk(): String =
        secureRandom.ints(KEY_LENGTH, 0, ALLOWED_CHARACTERS.length).toArray()
            .fold("") { acc, next -> acc + ALLOWED_CHARACTERS[next] }

    fun changeActiveKey(identity: String) {
        val currentPsk =
            pskRepository.findLatestPskForIdentityWithStatus(identity, PreSharedKeyStatus.ACTIVE)
        val newPsk =
            pskRepository.findLatestPskForIdentityWithStatus(identity, PreSharedKeyStatus.PENDING)

        check(currentPsk != null && newPsk != null) { "No current or new psk, impossible to change active key" }

        currentPsk.status = PreSharedKeyStatus.INACTIVE
        newPsk.status = PreSharedKeyStatus.ACTIVE
        pskRepository.save(currentPsk)
        pskRepository.save(newPsk)
    }
}
