// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

rootProject.name = "sng-crest-device-service"

include("application")
include("components:avro")
include("components:psk")
include("components:device")
include("components:firmware")
include("components:shared")
include("components:base85")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlinLogging", "7.0.0")
            version("avro", "1.12.0")
            version("mockk", "1.13.13")
            version("springmockk", "4.0.2")
            version("commonsCodec", "1.17.1")
            version("gxfUtils", "2.1")

            library("logging", "io.github.oshai", "kotlin-logging-jvm").versionRef("kotlinLogging")

            library("avro", "org.apache.avro", "avro").versionRef("avro")

            library("kafkaAvro", "com.gxf.utilities", "kafka-avro").versionRef("gxfUtils")
            library("kafkaAzureOauth", "com.gxf.utilities", "kafka-azure-oauth").versionRef("gxfUtils")
            bundle("gxfUtils", listOf("kafkaAvro", "kafkaAzureOauth"))

            library("mockk", "io.mockk", "mockk").versionRef("mockk")
            library("springmockk", "com.ninja-squad", "springmockk").versionRef("springmockk")

            library("commonsCodec", "commons-codec", "commons-codec").versionRef("commonsCodec")
        }
    }
}
