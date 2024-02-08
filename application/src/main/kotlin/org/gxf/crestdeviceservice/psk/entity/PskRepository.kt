package org.gxf.crestdeviceservice.psk.entity

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PskRepository : CrudRepository<PreSharedKey, PreSharedKeyCompositeKey> {

    fun findFirstByIdentityOrderByRevisionDesc(identity: String): PreSharedKey?

    fun countPsksByIdentity(identity: String): Long
}
