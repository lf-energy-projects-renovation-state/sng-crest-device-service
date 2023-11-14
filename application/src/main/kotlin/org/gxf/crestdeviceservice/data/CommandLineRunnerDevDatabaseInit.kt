package org.gxf.crestdeviceservice.data

import org.flywaydb.core.Flyway
import org.gxf.crestdeviceservice.data.entity.Psk
import org.gxf.crestdeviceservice.psk.PskRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.Instant

@Component
@Profile("dev")
class CommandLineRunnerDevDatabaseInit(private val pskRepository: PskRepository,
                                       private val dataSourceProperties: DataSourceProperties) : CommandLineRunner {

    @Value("\${crest-device-service.database.dev-initial-psk}")
    private lateinit var initialPsk: String

    @Value("\${crest-device-service.database.dev-device-identity}")
    private lateinit var deviceIdentity: String

    /***
     * Setup function for local development.
     * Runs flyway migrations manual, because the CommandLineRunner is run before spring runs the flyway migrations automatically.
     * Sets the psk used by the simulator if it doesn't exist.
     */
    override fun run(vararg args: String?) {
        val flyway = Flyway.configure()
                .locations("db/migrations")
                .dataSource(dataSourceProperties.url, dataSourceProperties.username, dataSourceProperties.password)
                .load()

        flyway.migrate()

        val count = pskRepository.countPsksByIdentity(deviceIdentity)

        if (count == 0L) {
            pskRepository.save(Psk(deviceIdentity, Instant.now(), initialPsk))
        }
    }
}
