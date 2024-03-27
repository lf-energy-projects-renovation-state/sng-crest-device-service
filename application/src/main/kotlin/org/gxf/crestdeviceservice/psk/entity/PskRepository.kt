package org.gxf.crestdeviceservice.psk.entity

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PskRepository : CrudRepository<PreSharedKey, PreSharedKeyCompositeKey> {

    fun findFirstByIdentityAndStatusOrderByRevisionDesc(identity: String): PreSharedKey?

    fun findFirstByIdentityOrderByRevisionDesc(identity: String): PreSharedKey?

    fun findFirstByIdentityOrderByRevisionAsc(identity: String): PreSharedKey?

    fun countByIdentityAndStatus(identity: String, status: PreSharedKeyStatus): Long
}
