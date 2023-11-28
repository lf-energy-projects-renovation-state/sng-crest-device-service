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
    implementation(libs.logging)

    implementation(libs.bundles.data)

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot-starter-logging")

    implementation("org.springframework:spring-aspects")

    implementation("org.springframework.kafka:spring-kafka")
    implementation(libs.microsoftMsal)
    implementation(libs.bundles.gxfUtils)

    implementation(project(":components:avro-measurement"))

    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.assertJ)
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
                implementation("org.springframework.boot:spring-boot-starter-data-jpa")
                implementation(libs.kafkaAvro)
                implementation("org.springframework.kafka:spring-kafka")
                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("org.springframework.kafka:spring-kafka-test")
                implementation(integrationTestLibs.kafkaTestContainers)
                implementation("org.springframework.ws:spring-ws-test")
                runtimeOnly(integrationTestLibs.h2)
                implementation(libs.mockitoKotlin)
            }
        }
    }
}
