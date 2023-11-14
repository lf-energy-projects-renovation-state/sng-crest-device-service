package org.gxf.crestdeviceservice.psk

import org.gxf.crestdeviceservice.data.entity.Psk
import org.gxf.crestdeviceservice.data.entity.PskKey
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PskRepository : CrudRepository<Psk, PskKey> {

    fun findFirstByIdentityOrderByRevisionTimeStampDesc(identity: String): Psk?

    fun countPsksByIdentity(identity: String): Long
}
