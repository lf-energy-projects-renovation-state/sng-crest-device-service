package org.gxf.crestdeviceservice.psk

import io.github.oshai.kotlinlogging.KotlinLogging
import org.flywaydb.core.Flyway
import org.gxf.crestdeviceservice.psk.entity.PreSharedKey
import org.gxf.crestdeviceservice.psk.entity.PskRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.stereotype.Component
import java.time.Instant

@Component
@ConditionalOnProperty(name = ["crest-device-service.database.set-initial-psk"], havingValue = "true")
class DevDatabaseInitCommandLineRunner(private val pskRepository: PskRepository,
                                       private val dataSourceProperties: DataSourceProperties) : CommandLineRunner {

    private val logger = KotlinLogging.logger {}

    @Value("\${crest-device-service.database.dev-initial-psk}")
    private lateinit var initialPsk: String

    @Value("\${crest-device-service.database.dev-initial-secret}")
    private lateinit var initialSecret: String

    @Value("\${crest-device-service.database.dev-device-identity}")
    private lateinit var deviceIdentity: String

    /***
     * Setup function for local development.
     * Runs flyway migrations manually, because the CommandLineRunner is run before spring runs the flyway migrations automatically.
     * Sets the psk used by the simulator if it doesn't exist.
     */
    override fun run(vararg args: String?) {
        val flyway = Flyway
                .configure()
                .dataSource(dataSourceProperties.url, dataSourceProperties.username, dataSourceProperties.password)
                .load()

        flyway.migrate()

        val count = pskRepository.countPsksByIdentity(deviceIdentity)

        if (count == 0L) {
            logger.warn { "Creating Pre Shared Key for identity: $deviceIdentity" }
            pskRepository.save(PreSharedKey(deviceIdentity, Instant.now(), initialPsk, initialSecret))
        }
    }
}
