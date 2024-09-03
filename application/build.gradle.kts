import org.springframework.boot.gradle.tasks.bundling.BootJar

// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("org.springframework.boot")
    id("com.github.davidmc24.gradle.plugin.avro")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.security:spring-security-core")
    implementation("org.springframework.kafka:spring-kafka")

    implementation(project(":components:psk"))

    implementation(kotlin("reflect"))
    implementation(libs.logging)

    implementation(libs.bundles.gxfUtils)

    implementation(libs.commonsCodec)

    implementation(libs.avro)

    implementation(libs.ascii85)

    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockitoKotlin)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Generate test and integration test reports
    jacocoAggregation(project(":application"))
}

tasks.withType<BootJar> {
    // Exclude test keys and certificates
    exclude("ssl/*.pem")
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootBuildImage> {
    imageName.set("ghcr.io/osgp/gxf-sng-crest-device-service:${version}")
    if (project.hasProperty("publishImage")) {
        publish.set(true)
        docker {
            publishRegistry {
                username.set(System.getenv("GITHUB_ACTOR"))
                password.set(System.getenv("GITHUB_TOKEN"))
            }
        }
    }
}

testing {
    suites {
        val integrationTest by registering(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(project())
                implementation(project(":components:psk"))
                implementation("org.springframework.boot:spring-boot-starter-data-jpa")
                implementation(libs.kafkaAvro)
                implementation(libs.avro)
                implementation("org.springframework.kafka:spring-kafka")
                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("org.springframework.kafka:spring-kafka-test")
                implementation("org.testcontainers:kafka")
                implementation("org.springframework.ws:spring-ws-test")
                implementation(libs.mockitoKotlin)
                runtimeOnly("com.h2database:h2")
            }
        }
    }
}
