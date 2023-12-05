package org.gxf.crestdeviceservice.psk

import org.gxf.crestdeviceservice.data.entity.PreSharedKey
import org.gxf.crestdeviceservice.data.entity.PreSharedKeyCompositeKey
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PskRepository : CrudRepository<PreSharedKey, PreSharedKeyCompositeKey> {

    fun findFirstByIdentityOrderByRevisionTimeDesc(identity: String): PreSharedKey?

    fun countPsksByIdentity(identity: String): Long
}
