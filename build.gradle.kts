// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask
import io.spring.gradle.dependencymanagement.internal.dsl.StandardDependencyManagementExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.3.2" apply false
    id("io.spring.dependency-management") version "1.1.5" apply false
    kotlin("jvm") version "2.0.0" apply false
    kotlin("plugin.spring") version "2.0.0" apply false
    kotlin("plugin.jpa") version "2.0.0" apply false
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1" apply false
    id("com.diffplug.spotless") version "6.25.0"
    id("org.sonarqube") version "5.0.0.4638"
    id("eclipse")
}

version = System.getenv("GITHUB_REF_NAME")?.replace("/", "-")?.lowercase() ?: "develop"

sonar {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectKey", "OSGP_sng-crest-device-service")
        property("sonar.organization", "gxf")
        property("sonar.gradle.skipCompile", true)
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "eclipse")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "jacoco")
    apply(plugin = "jacoco-report-aggregation")

    group = "org.gxf.deviceservice"
    version = rootProject.version

    repositories {
        mavenCentral()
        maven {
            name = "GXFGithubPackages"
            url = uri("https://maven.pkg.github.com/osgp/*")
            credentials {
                username = project.findProperty("github.username") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("github.token") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }

    extensions.configure<SpotlessExtension> {
        kotlin {
            // by default the target is every '.kt' and '.kts' file in the java source sets
            ktfmt().dropboxStyle()
            licenseHeaderFile(
                "${project.rootDir}/license-template.kt",
                "package")
                .updateYearWithLatest(false)
        }
    }

    extensions.configure<StandardDependencyManagementExtension> {
        imports { mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES) }
    }

    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
        compilerOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    tasks.withType<KotlinCompile> {
        dependsOn(tasks.withType<GenerateAvroJavaTask>())
    }

    tasks.withType<Test> { useJUnitPlatform() }
}
