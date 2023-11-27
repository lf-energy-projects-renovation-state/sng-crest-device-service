// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")


    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework:spring-aop")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    implementation("org.postgresql:postgresql:42.5.4")
    implementation("org.flywaydb:flyway-core:9.22.3")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot-starter-logging")

    implementation("org.springframework:spring-aspects")

    implementation("org.springframework.kafka:spring-kafka")
    implementation("com.microsoft.azure:msal4j:1.13.10")

    implementation("com.gxf.utilities:kafka-avro:0.2")
    implementation("com.gxf.utilities:kafka-azure-oauth:0.2")
    implementation(project(":components:avro-measurement"))

    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")

    // Generate test and integration test reports
    jacocoAggregation(project(":application"))
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
                implementation(project(":components:avro-measurement"))
                implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.1.5")
                implementation("com.gxf.utilities:kafka-avro:0.2")
                implementation("org.springframework.kafka:spring-kafka")
                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("org.springframework.kafka:spring-kafka-test")
                implementation("org.testcontainers:kafka:1.17.6")
                implementation("org.springframework.ws:spring-ws-test")
                runtimeOnly("com.h2database:h2:2.2.224")
                implementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
            }
        }
    }
}
