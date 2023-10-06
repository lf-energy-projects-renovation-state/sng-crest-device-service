// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework:spring-aop")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation(project(":components:kafka"))

    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("org.springframework:spring-aspects")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootBuildImage> {
    imageName.set("ghcr.io/osgp/gxf-service-template:${version}")
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
                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("org.springframework.kafka:spring-kafka-test")
                implementation("org.testcontainers:kafka:1.17.6")
                implementation("org.springframework.ws:spring-ws-test")
            }
        }
    }
}
