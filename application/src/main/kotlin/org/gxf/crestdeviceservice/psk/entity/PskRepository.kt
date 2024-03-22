package org.gxf.crestdeviceservice.psk.entity

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PskRepository : CrudRepository<PreSharedKey, PreSharedKeyCompositeKey> {

    @Query("""
        select psk from  PreSharedKey psk 
        where psk.identity = ?1 
          and psk.status = ?2
        order by psk.revision desc
        limit 1
        """)
    fun findLatestPskForIdentityWithStatus(
        identity: String,
        status: PreSharedKeyStatus
    ): PreSharedKey?

    @Query("""
        select psk from PreSharedKey psk 
        where psk.identity = ?1 
        order by psk.revision desc
        limit 1
        """)
    fun findLatestPsk(identity: String): PreSharedKey?

    @Query(
        """
        select psk from PreSharedKey psk 
        where psk.identity = ?1 
        order by psk.revision asc
        limit 1
        """
    )
    fun findOldestPsk(identity: String): PreSharedKey?

    @Query("""
        select count(psk) from PreSharedKey psk 
        where psk.identity = ?1
         and psk.status = ?2
        """)
    fun countPSKsForIdWithStatus(identity: String, status: PreSharedKeyStatus): Long
}
