package org.gxf.crestdeviceservice.psk.entity

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PskRepository : CrudRepository<PreSharedKey, PreSharedKeyCompositeKey> {

    @Query("""
        select psk from  PreSharedKey psk 
        where psk.identity = ?1 
          and psk.status = 'ACTIVE'
        order by psk.revision desc
        """)
    fun findLatestActivePsk(identity: String): PreSharedKey?

    @Query("""
        select psk from PreSharedKey psk 
        where psk.identity = ?1 
        order by psk.revision desc
        """)
    fun findLatestPsk(identity: String): PreSharedKey?

    @Query(
        """
        select psk from PreSharedKey psk 
        where psk.identity = ?1 
        order by psk.revision asc
        """
    )
    fun findOldestPsk(identity: String): PreSharedKey?

    @Query("""
        select count(psk) from PreSharedKey psk 
        where psk.identity = ?1
         and (psk.status = 'ACTIVE'
           or psk.status = 'INACTIVE')
        """)
    fun countActiveAndInactivePSKs(identity: String): Long
}
