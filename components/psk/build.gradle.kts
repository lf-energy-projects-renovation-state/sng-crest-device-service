// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.security:spring-security-core")

    implementation(project(":components:shared"))

    implementation(libs.commonsCodec)
    implementation(libs.logging)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockk)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
