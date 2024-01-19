package org.gxf.crestdeviceservice.psk.entity

import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PreSharedKeyCompositeKey
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PskRepository : CrudRepository<PreSharedKey, PreSharedKeyCompositeKey> {

    fun findFirstByIdentityOrderByRevisionTimeDesc(identity: String): PreSharedKey?

    fun countPsksByIdentity(identity: String): Long
}
