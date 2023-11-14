package org.gxf.crestdeviceservice.data.entity

import jakarta.persistence.*
import org.gxf.crestdeviceservice.data.convertors.DatabaseFieldEncryptor
import java.time.Instant

@Entity
@IdClass(PskKey::class)
class Psk(@Id val identity: String,
          @Id val revisionTimeStamp: Instant,
          @Convert(converter = DatabaseFieldEncryptor::class) @Column(name = "`key`") val key: String
)
